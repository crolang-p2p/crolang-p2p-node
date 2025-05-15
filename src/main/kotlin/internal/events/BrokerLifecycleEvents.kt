/*
 * Copyright 2025 Alessandro Talmi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package internal.events

import com.google.gson.JsonParser
import internal.events.data.RTCConfigurationMsg
import internal.broker.ConnectionToBrokerErrorReason
import internal.broker.BrokerSocketCreator.createSocketIO
import internal.utils.Event
import internal.utils.SharedStore
import internal.utils.SharedStore.executeCallbackOnExecutor
import internal.utils.SharedStore.flush
import internal.utils.SharedStore.brokerLifecycleCallbacks
import internal.utils.SharedStore.logger
import internal.utils.SharedStore.reconnectionAttempts
import internal.utils.SharedStore.rtcConfiguration
import internal.utils.SharedStore.settings
import internal.utils.SharedStore.socketIO
import io.socket.engineio.client.EngineIOException
import org.crolangP2P.InvoluntaryBrokerDisconnectionCause
import java.util.*

/**
 * This event is called when receiving the AUTHENTICATED message from the Broker after a successful connection;
 * the Broker will always send an AUTHENTICATED message after a successful connection attempt.
 * The message contains the RTC configuration for the P2P connection.
 */
internal class OnValidAuthenticationMsg(private val msg: RTCConfigurationMsg): Event {

    override fun process() {
        logger.debugInfo("Received RTC configuration")
        rtcConfiguration = Optional.of(msg.toConcreteRTCConfiguration())
        if(SharedStore.brokerConnectionHelper.connectionToBrokerGuard.isCountdownInProgress()){
            onVoluntaryConnectionToBrokerAttempt()
        } else {
            onSuccessfulAutomaticReconnectionAttempt()
        }
    }

    /**
     * This method is called when the connection to the Broker is voluntary (by CrolangP2P.connectToBroker()).
     * It counts down the connection latch in order to progress with the sync connectToBroker method.
     */
    private fun onVoluntaryConnectionToBrokerAttempt(){
        logger.regularInfo("connected to Broker")
        SharedStore.brokerConnectionHelper.countDownConnectionLatch()
    }

    /**
     * This method is called when the automatic reconnection to the Broker is successful.
     * This can only happen if the reconnection is enabled by settings.
     * It notifies the user by calling the user-defined callback.
     */
    private fun onSuccessfulAutomaticReconnectionAttempt(){
        logger.regularInfo("reconnected to Broker")
        executeCallbackOnExecutor {
            brokerLifecycleCallbacks.onSuccessfullyReconnected()
        }
    }

}

/**
 * This event is called when the AUTHENTICATED message from the Broker is not valid.
 * It can happen if the message is not in the expected format.
 */
internal class OnAuthenticationMsgParsingError: Event {

    override fun process() {
        logger.regularErr("error on parsing RTC configuration")
        if(socketIO.isPresent && socketIO.get().connected()){
            socketIO.get().close()
            socketIO = Optional.empty()
        }
        if(SharedStore.brokerConnectionHelper.connectionToBrokerGuard.isCountdownInProgress()){
            SharedStore.brokerConnectionHelper.countDownConnectionLatch(
                ConnectionToBrokerErrorReason.ERROR_PARSING_RTC_CONFIGURATION
            )
        }
    }

}

/**
 * This event is called when a connection attempt to the Broker fails; this corresponds to the socket's connect error event.
 * It handles both voluntary and involuntary connection attempts.
 * The connection attempt can, from the Node point of view, be voluntary (by CrolangP2P.connectToBroker()) or
 * involuntary (by the server or by the network).
 * The class also handles the reconnection attempts if enabled by settings.
 */
internal class OnBrokerConnectError(private val connectErrorPayload: Array<Any>): Event {

    override fun process() {
        val socket = socketIO.get()
        socketIO = Optional.empty()
        val connectionToBrokerErrorReason = getConnectionToBrokerErrorReason()
        socket.close()
        if(SharedStore.brokerConnectionHelper.connectionToBrokerGuard.isCountdownInProgress()){
            onVoluntaryConnectionAttemptConnectionError(connectionToBrokerErrorReason)
        } else if(settings.reconnection){
            onReconnectionAttemptConnectionError(connectionToBrokerErrorReason)
        }
    }

    /**
     * This method is called when the connection to the Broker fails.
     * It tries to determine the reason for the connection failure.
     * The method checks if the error payload is not empty and tries to parse it as a JSON object (this is the Broker that
     * disconnected the Node and sent a message explaining the reason).
     * If the parsing fails, it assumes a socket error (network error or the server is down).
     * If the parsing succeeds, it checks for specific error messages and sets the appropriate error reason.
     */
    private fun getConnectionToBrokerErrorReason(): ConnectionToBrokerErrorReason {
        var connectionToBrokerErrorReason: ConnectionToBrokerErrorReason = ConnectionToBrokerErrorReason.UNKNOWN_ERROR
        if(connectErrorPayload.iterator().hasNext()){
            val error = connectErrorPayload.iterator().next()
            try {
                val brokerHandledMsg = JsonParser.parseString(error.toString()).asJsonObject["message"]?.asString
                if (brokerHandledMsg == "authentication failed") {
                    connectionToBrokerErrorReason = ConnectionToBrokerErrorReason.UNAUTHORIZED
                } else if (brokerHandledMsg == "client already connected") {
                    connectionToBrokerErrorReason = ConnectionToBrokerErrorReason.CLIENT_WITH_SAME_ID_ALREADY_CONNECTED
                }
                logger.regularErr("error while connecting to the Broker: $brokerHandledMsg")
            } catch (e: Exception) {
                val exception = error as? EngineIOException
                if (exception == null) {
                    logger.regularErr("error while connecting to the Broker: $error")
                } else {
                    connectionToBrokerErrorReason = ConnectionToBrokerErrorReason.SOCKET_ERROR
                    logger.regularErr("error while connecting to the Broker: ${exception.message}")
                }
            }
        } else {
            logger.regularErr("error while connecting to the Broker")
        }
        return connectionToBrokerErrorReason
    }

    /**
     * This method is called when the connection to the Broker is voluntary (by CrolangP2P.connectToBroker()).
     * It counts down the connection latch in order to progress with the sync connectToBroker method.
     */
    private fun onVoluntaryConnectionAttemptConnectionError(connectionToBrokerErrorReason: ConnectionToBrokerErrorReason){
        SharedStore.brokerConnectionHelper.countDownConnectionLatch(connectionToBrokerErrorReason)
    }

    /**
     * This method is called when the reconnection to the Broker is attempted.
     * When the error is not a socket error, reconnection is not possible.
     */
    private fun onReconnectionAttemptConnectionError(connectionToBrokerErrorReason: ConnectionToBrokerErrorReason){
        if(connectionToBrokerErrorReason == ConnectionToBrokerErrorReason.SOCKET_ERROR){
            onReconnectionAttemptSocketError()
        } else {
            onReconnectionAttemptNotPossible(connectionToBrokerErrorReason)
        }
    }

    /**
     * This method is called when the reconnection to the Broker is attempted and the error is a socket error.
     * It tries to reconnect to the Broker after a delay, unless the maximum number of reconnection attempts is exceeded.
     */
    private fun onReconnectionAttemptSocketError(){
        logger.regularInfo("reconnecting to the Broker...")
        if(settings.maxReconnectionAttempts.isPresent){
            if(reconnectionAttempts >= settings.maxReconnectionAttempts.get()){
                logger.regularInfo("reconnection attempts exceeded, not reconnecting")
                flush()
                executeCallbackOnExecutor {
                    brokerLifecycleCallbacks.onInvoluntaryDisconnection(
                        InvoluntaryBrokerDisconnectionCause.MAX_RECONNECTION_ATTEMPTS_EXCEEDED
                    )
                }
                return
            }
        }
        reconnectionAttempts++
        brokerLifecycleCallbacks.onReconnectionAttempt()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                socketIO = Optional.of(createSocketIO())
                socketIO.get().connect()
            }
        }, settings.reconnectionAttemptsDeltaMs)
    }

    /**
     * This method is called when the reconnection to the Broker is attempted and the error is not a socket error.
     * It notifies the involuntary disconnection to the user by calling the user-defined callback.
     */
    private fun onReconnectionAttemptNotPossible(connectionToBrokerErrorReason: ConnectionToBrokerErrorReason){
        logger.regularInfo("reconnecting to the Broker failed by $connectionToBrokerErrorReason, not reconnecting")
        flush()
        executeCallbackOnExecutor {
            brokerLifecycleCallbacks.onInvoluntaryDisconnection(
                connectionToBrokerErrorReason.toInvoluntaryBrokerDisconnectionCause()
            )
        }
    }

}

/**
 * This event is called when the disconnection from the Broker occurs; this corresponds to the socket's disconnect event.
 * It handles both voluntary and involuntary disconnections.
 * The disconnection can be voluntary (by CrolangP2P.disconnectFromBroker()) or involuntary (by the server or by the network).
 * The class also handles the reconnection attempts if enabled by settings.
 */
internal class OnBrokerDisconnection: Event {

    override fun process() {
        if(SharedStore.brokerConnectionHelper.disconnectionFromBrokerGuard.isCountdownInProgress()){
            onVoluntaryDisconnection()
        } else {
            onInvoluntaryDisconnection()
        }
    }

    /**
     * This method is called when the disconnection from the Broker is voluntary (by CrolangP2P.disconnectFromBroker()).
     */
    private fun onVoluntaryDisconnection() {
        logger.regularInfo("disconnected from Broker voluntarily")
        SharedStore.brokerConnectionHelper.disconnectionFromBrokerGuard.stepDown()
    }

    /**
     * This method is called when the disconnection from the Broker is involuntary (by the server or by the network).
     */
    private fun onInvoluntaryDisconnection() {
        if(settings.reconnection){
            onReconnectionEnabledBySettings()
        } else {
            onReconnectionDisabledBySettings()
        }
    }

    /**
     * This method is called when the disconnection from the Broker is involuntary and the reconnection is enabled by settings.
     * It tries to reconnect to the Broker after a delay.
     */
    private fun onReconnectionEnabledBySettings(){
        if(settings.maxReconnectionAttempts.isPresent){
            if(reconnectionAttempts >= settings.maxReconnectionAttempts.get()){
                logger.regularInfo("reconnection attempts exceeded, not reconnecting")
                flush()
                executeCallbackOnExecutor {
                    brokerLifecycleCallbacks.onInvoluntaryDisconnection(
                        InvoluntaryBrokerDisconnectionCause.MAX_RECONNECTION_ATTEMPTS_EXCEEDED
                    )
                }
                return
            }
        }
        logger.regularInfo("involuntarily disconnected from Broker, reconnecting...")
        reconnectionAttempts++
        executeCallbackOnExecutor {
            brokerLifecycleCallbacks.onReconnectionAttempt()
        }
        Timer().schedule(object : TimerTask() {
            override fun run() {
                socketIO = Optional.of(createSocketIO())
                socketIO.get().connect()
            }
        }, settings.reconnectionAttemptsDeltaMs)
    }

    /**
     * This method is called when the disconnection from the Broker is involuntary and the reconnection is disabled by settings.
     * It notifies the involuntary disconnection to the user by calling the user-defined callback.
     */
    private fun onReconnectionDisabledBySettings(){
        logger.regularInfo("involuntarily disconnected from Broker, not reconnecting")
        flush()
        executeCallbackOnExecutor {
            brokerLifecycleCallbacks.onInvoluntaryDisconnection(InvoluntaryBrokerDisconnectionCause.UNKNOWN_ERROR)
        }
    }

}
