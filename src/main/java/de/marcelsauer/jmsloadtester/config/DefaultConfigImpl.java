package de.marcelsauer.jmsloadtester.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.marcelsauer.jmsloadtester.core.Constants;
import de.marcelsauer.jmsloadtester.core.JmsException;
import de.marcelsauer.jmsloadtester.message.MessageContentStrategy;
import de.marcelsauer.jmsloadtester.message.MessageContentStrategyFactory;
import de.marcelsauer.jmsloadtester.message.MessageInterceptor;
import de.marcelsauer.jmsloadtester.output.OutputStrategy;
import de.marcelsauer.jmsloadtester.output.OutputStrategyFactory;
import de.marcelsauer.jmsloadtester.tools.Logger;
import de.marcelsauer.jmsloadtester.tools.PropertyUtils;
import de.marcelsauer.jmsloadtester.tools.StringUtils;

/**
 * JMS Load Tester Copyright (C) 2008 Marcel Sauer
 * <marcel[underscore]sauer[at]gmx.de>
 * 
 * This file is part of JMS Load Tester.
 * 
 * JMS Load Tester is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * JMS Load Tester is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * JMS Load Tester. If not, see <http://www.gnu.org/licenses/>.
 */
public class DefaultConfigImpl implements Config {

    private MessageContentStrategyFactory messageContentStrategyFactory;

    // propery keys
    private static final String APP_PREFIX = "app.";

    private static final String SPLITTER = ",";

    private static final String SUBSCRIBERS_TO_START = APP_PREFIX + "listener.thread.count";
    private static final String PUBLISHERS_TO_START = APP_PREFIX + "sender.threads.to.start";
    private static final String SUBSCRIBER_WAIT_FOR = APP_PREFIX + "listener.wait.for.message.count";
    private static final String MESSAGE_CONTENT_STRATEGY = APP_PREFIX + "sender.message.content.strategy";
    private static final String LISTEN_TO_DEST = APP_PREFIX + "listener.listen.to.destination";
    private static final String SEND_TO_DEST = APP_PREFIX + "sender.send.to.destination";
    private static final String PUB_SLEEP = APP_PREFIX + "sender.pause.millis.between.send";
    private static final String DEBUG_OUT_STRATEGY = APP_PREFIX + "output.debug.strategy";
    private static final String RESULT_OUT_STRATEGY = APP_PREFIX + "output.result.strategy";
    private static final String MESSAGE_OUT_STRATEGY = APP_PREFIX + "output.message.strategy";
    private static final String PAUSE_PROGRESS = APP_PREFIX + "output.pause.seconds.between.printing.progress";
    private static final String LISTENER_RAMPUP = APP_PREFIX + "listener.ramp.up.millis";
    private static final String SENDER_RAMPUP = APP_PREFIX + "sender.ramp.up.millis";
    private static final String MESSAGE_INTERCEPTORS = APP_PREFIX + "message.interceptors";
    private static final String LISTENER_ACK_MESSAGE = APP_PREFIX + "listener.explicit.acknowledge.message";

    // connection factory
    private static final String CONNECTION_FACTORY = "javax.jms.ConnectionFactory";

    private static final String DELIVERY_MODE = "javax.jms.delivery.mode";
    private static final String PRIORITY = "javax.jms.message.producer.priority";
    private static final String TIME_TO_LIVE = "javax.jms.message.producer.time.to.live";

    // set via config file
    private int subscribersToStart;
    private int publishersToStart;
    private int messagesToSend;
    private int pubSleepMillis;
    private int subscriberWaitFor;
    private int eachSubscriberWaitFor;
    private int pauseBetweenPrintProgress;
    private int listenerRampup;
    private int senderRampup;
    private int expectedMessageSentCount;
    private int priority;
    private long timeToLive;

    private boolean createJndiDestinationIfNotFound;
    private boolean listenerExplicitAckMessage;

    private String connectionFactory;
    private String listenToDestination;
    private String sendToDestination;
    private String messageContentStrategy;
    private String deliveryMode;

    private OutputStrategy debugOutputStrategy;
    private OutputStrategy resultOutputStrategy;
    private OutputStrategy messageOutputStrategy;

    private List<MessageInterceptor> messageInterceptors = new ArrayList<MessageInterceptor>();

    private Properties properties;

    public DefaultConfigImpl(final Properties applicationProperties, final MessageContentStrategyFactory messageContentStrategyFactory) {
        this.messageContentStrategyFactory = messageContentStrategyFactory;
        loadConfig(applicationProperties);
    }

    public DefaultConfigImpl(final String filename, final MessageContentStrategyFactory messageContentStrategyFactory) {
        this.messageContentStrategyFactory = messageContentStrategyFactory;
        loadConfig(filename);
    }

    public void loadConfig(final String filename) {
        Logger.info("using config file: " + filename);
        Properties properties = PropertyUtils.loadProperties(filename);
        loadConfig(properties);
    }

    public void loadConfig(final Properties config) {
        Logger.info("using properties: " + config);
        this.properties = config;
        try {
            initValues();
        } catch (NumberFormatException e) {
            Logger.error("could not load all the values from the properties file. did you provide all values with valid values?", e);
        }
    }

    private void initValues() {
        try {
            // straight forward stuff
            subscribersToStart = parseInt(SUBSCRIBERS_TO_START);
            publishersToStart = parseInt(PUBLISHERS_TO_START);
            pubSleepMillis = parseInt(PUB_SLEEP);
            eachSubscriberWaitFor = parseInt(SUBSCRIBER_WAIT_FOR);
            listenerRampup = parseInt(LISTENER_RAMPUP);
            senderRampup = parseInt(SENDER_RAMPUP);
            priority = parseInt(PRIORITY);
            timeToLive = parseLong(TIME_TO_LIVE);

            connectionFactory = parseString(CONNECTION_FACTORY);
            listenToDestination = parseString(LISTEN_TO_DEST);
            sendToDestination = parseString(SEND_TO_DEST);
            messageContentStrategy = parseString(MESSAGE_CONTENT_STRATEGY);
            deliveryMode = parseString(DELIVERY_MODE);

            listenerExplicitAckMessage = parseBoolean(LISTENER_ACK_MESSAGE);

            // more "logic" parts
            pauseBetweenPrintProgress = parseInt(PAUSE_PROGRESS) * Constants.MILLIS_FACTOR;

            subscriberWaitFor = eachSubscriberWaitFor * subscribersToStart;
            
            setMessagesToSend(getMessageContentStrategy().getMessageCount());

            debugOutputStrategy = OutputStrategyFactory.getOutputStrategy(parseString(DEBUG_OUT_STRATEGY));
            resultOutputStrategy = OutputStrategyFactory.getOutputStrategy(parseString(RESULT_OUT_STRATEGY));
            messageOutputStrategy = OutputStrategyFactory.getOutputStrategy(parseString(MESSAGE_OUT_STRATEGY));

            parseInterceptors(getStringValue(properties.get(MESSAGE_INTERCEPTORS)));

        } catch (IllegalStateException e) {
            Logger.error("the configuration file seems to be incorrect", e);
        }
    }

    private int parseInt(String key) {
        return getMandatoryIntValue(properties.get(key));
    }

    private String parseString(String key) {
        return getMandatoryStringValue(properties.get(key));
    }

    private long parseLong(String key) {
        return getMandatoryLongValue(properties.get(key));
    }

    private boolean parseBoolean(String key) {
        return getMandatoryBooleanValue(properties.get(key));
    }

    private long getMandatoryLongValue(final Object value) {
        check(value);
        return Long.valueOf((String) value);
    }

    private void parseInterceptors(String interceptors) {
        if (!StringUtils.isEmpty(interceptors)) {
            try {
                messageInterceptors = ClassParser.parseToInstances(interceptors, SPLITTER);
            } catch (Exception e) {
                throw new JmsException("could not parse interceptors: " + interceptors);
            }
        }
    }

    private void calculateExpectedMessageCounts() {
        expectedMessageSentCount = publishersToStart * messagesToSend;
    }

    private void check(final Object value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalStateException("the configuration value [" + value + "] is wrong. please set a correct one or comment the whole line");
        }
    }

    private boolean getMandatoryBooleanValue(final Object value) {
        check(value);
        return Boolean.valueOf((String) value);
    }

    private int getMandatoryIntValue(final Object value) {
        check(value);
        return Integer.valueOf((String) value);
    }

    private String getMandatoryStringValue(final Object value) {
        check(value);
        return String.valueOf(value);
    }

    private String getStringValue(final Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public int getSubscribersToStart() {
        return subscribersToStart;
    }

    public int getSendersToStart() {
        return publishersToStart;
    }

    public int getMessagesToSend() {
        return messagesToSend;
    }

    public String getConnectionFactory() {
        return connectionFactory;
    }

    // TODO move this somewhere else?
    // we always create a new one
    public MessageContentStrategy getMessageContentStrategy() {
        return messageContentStrategyFactory.getMessageContentStrategy(messageContentStrategy);
    }

    public String getListenToDestination() {
        return listenToDestination;
    }

    public String getSendToDestination() {
        return sendToDestination;
    }

    public int getPubSleepMillis() {
        return pubSleepMillis;
    }

    public OutputStrategy getDebugOutputStrategy() {
        return debugOutputStrategy;
    }

    public OutputStrategy getResultOutputStrategy() {
        return resultOutputStrategy;
    }

    public OutputStrategy getMessageOutputStrategy() {
        return messageOutputStrategy;
    }

    public int getExpectedMessageSentCount() {
        return expectedMessageSentCount;
    }

    public boolean doCreateDestinationIfNotExistent() {
        return createJndiDestinationIfNotFound;
    }

    public int getSubscriberWaitForTotalMessages() {
        return subscriberWaitFor;
    }

    public int getEachSubscriberWaitFor() {
        return eachSubscriberWaitFor;
    }

    public int getPauseBetweenPrintProgress() {
        return pauseBetweenPrintProgress;
    }

    public int getListenerRampup() {
        return listenerRampup;
    }

    public int getSenderRampup() {
        return senderRampup;
    }

    public void setMessageContentStrategyFactory(final MessageContentStrategyFactory messageContentStrategyFactory) {
        this.messageContentStrategyFactory = messageContentStrategyFactory;
    }

    private void setMessagesToSend(final int messagesToSend) {
        this.messagesToSend = messagesToSend;
        calculateExpectedMessageCounts();
    }

    public List<MessageInterceptor> getMessageInterceptors() {
        return messageInterceptors;
    }

    public boolean isExplicitAcknowledgeMessage() {
        return listenerExplicitAckMessage;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public int getPriority() {
        return priority;
    }

    public long getTimeToLive() {
        return timeToLive;
    }
}