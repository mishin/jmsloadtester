package de.marcelsauer.jmsloadtester.tracker;

import de.marcelsauer.jmsloadtester.client.Listener;
import de.marcelsauer.jmsloadtester.client.Sender;
import de.marcelsauer.jmsloadtester.config.Config;
import de.marcelsauer.jmsloadtester.message.MessageContentStrategy;
import de.marcelsauer.jmsloadtester.message.MessageContentStrategyWrapper;
import de.marcelsauer.jmsloadtester.message.MessageParser;
import de.marcelsauer.jmsloadtester.message.filter.PlaceHolderContentFilter;
import de.marcelsauer.jmsloadtester.spring.SpringFactory;

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
public class ThreadTrackerImpl implements ThreadTracker {

    private static int subscribersCreated;
    private static int publishersCreated;
    private MessageTracker messageTracker;
    private MessageParser messageParser;
    private Config config;

    public ThreadTrackerImpl(Config config) {
        this.config = config;
    }

    private MessageContentStrategy getMessageContentStrategy() {
        MessageContentStrategy target = config.getMessageContentStrategy();
        // @todo dodgy, remove this?
        PlaceHolderContentFilter filter = SpringFactory.getBean("placeholderContentFilter");
        // a new one for each thread !
        return new MessageContentStrategyWrapper(target, filter);
    }

    public void createListenerThread(final String name) {
        final Listener listener = new Listener();
        listener.setListenToDestination(config.getListenToDestination());
        listener.addMessageNotifyable(messageTracker);
        listener.setMessageOutStrategy(config.getMessageOutputStrategy());
        listener.setMessageParser(messageParser);
        listener.setExplicitAckMessage(config.isExplicitAcknowledgeMessage());
        
        final Thread subscriberThread = new Thread(listener);
        subscriberThread.setName(name + " [" + listener.hashCode() + "]");
        subscriberThread.start();
        subscribersCreated++;
    }

    public void createSenderThread(final String name) {
        final Sender sender = new Sender();
        sender.setDestination(config.getSendToDestination());
        sender.setSleepMilliseconds(config.getPubSleepMillis());
        sender.addMessageSentAware(messageTracker);
        sender.setMessageContentStrategy(getMessageContentStrategy());
        sender.setMessageInterceptors(config.getMessageInterceptors());
        
        final Thread senderThread = new Thread(sender);
        senderThread.setName(name + " [" + sender.hashCode() + "]");
        senderThread.start();
        publishersCreated++;
    }

    public synchronized int subscriberCreated() {
        return subscribersCreated++;
    }

    public synchronized int publisherCreated() {
        return publishersCreated++;
    }

    public synchronized int getListenersStarted() {
        return subscribersCreated;
    }

    public synchronized int getSendersStarted() {
        return publishersCreated;
    }

    public void setMessageTracker(final MessageTracker messageTracker) {
        this.messageTracker = messageTracker;
    }

    public void setMessageParser(final MessageParser messageParser) {
        this.messageParser = messageParser;
    }
}