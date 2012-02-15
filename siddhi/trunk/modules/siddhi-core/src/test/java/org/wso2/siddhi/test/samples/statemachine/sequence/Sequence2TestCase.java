/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.test.samples.statemachine.sequence;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.api.QueryFactory;
import org.wso2.siddhi.api.eventstream.InputEventStream;
import org.wso2.siddhi.api.eventstream.query.Query;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.EventImpl;
import org.wso2.siddhi.core.node.CallbackHandler;
import org.wso2.siddhi.core.node.InputHandler;

import static org.wso2.siddhi.api.condition.where.ConditionOperator.GREATERTHAN_EQUAL;
import static org.wso2.siddhi.api.condition.where.ConditionOperator.LESSTHAN;

public class Sequence2TestCase {

    private static final Logger log = Logger.getLogger(Sequence2TestCase.class);
    private volatile int i;
    private volatile boolean eventCaptured;

    @Before
    public void info() {
        log.debug("-----Query processed: Testing sequence with star-----");
        i = 0;
        eventCaptured = false;
    }

    @Test
    public void testAPI() throws Exception {
        //Instantiate SiddhiManager
        SiddhiManager siddhiManager = new SiddhiManager();

        QueryFactory qf = SiddhiManager.getQueryFactory();

        InputEventStream stockQuoteStream = new InputEventStream(
                "StockQuoteStream",
                new String[]{"timestamp", "symbol", "price", "volume"},
                new Class[]{Integer.class, String.class, Integer.class, Integer.class}
        );

        siddhiManager.addInputEventStream(stockQuoteStream);

        //stock+ a[ ], stock b
        Query query = qf.createQuery(
                "StockQuote",
                qf.output("priceA=$0.price", "priceB=$1.last.price", "priceA=$2.price"),
                qf.inputStreams(qf.from(stockQuoteStream)),
                qf.sequence(
                        qf.condition("StockQuoteStream.price", LESSTHAN, "500"),
                        qf.star(
                                qf.condition("StockQuoteStream.price", LESSTHAN, "500")
                        ),
                        qf.condition("StockQuoteStream.price", GREATERTHAN_EQUAL, "500")
                )
        );

        siddhiManager.addQuery(query);

        siddhiManager.addCallback(assignCallback());
        siddhiManager.update();

        InputHandler inputHandlerStockQuoteStream = siddhiManager.getInputHandler("StockQuoteStream");
        sendEvents(inputHandlerStockQuoteStream);

        assertTest();
        siddhiManager.shutDownTask();
    }
    @Test
    public void testQuery() throws Exception {
        //Instantiate SiddhiManager
        SiddhiManager siddhiManager = new SiddhiManager();

        siddhiManager.addQueries("StockQuoteStream:= timestamp[int], symbol [string], price [int], volume [int]; \n" +
                                 "" +
                                 "StockQuote:=select priceA=$a1.price,priceA=$a2[last].price, priceC=$a3.price \n" +
                                 "from StockQuoteStream \n" +
                                 "sequence [a1=StockQuoteStream.price<500,\n" +
                                 "a2=StockQuoteStream.price < 500, \n" +
                                 "a3=StockQuoteStream.price >= 500] \n" +
                                 "$a1 $a2* $a3 ;");

        siddhiManager.addCallback(assignCallback());
        siddhiManager.update();

        InputHandler inputHandlerStockQuoteStream = siddhiManager.getInputHandler("StockQuoteStream");
        sendEvents(inputHandlerStockQuoteStream);

        assertTest();
        siddhiManager.shutDownTask();
    }

    private CallbackHandler assignCallback() {
        return new CallbackHandler("StockQuote") {
            public void callBack(Event event) {
                log.debug("       Event captured  " + event + " ");
                if ((Integer) event.getNthAttribute(1) == 100) {
                    eventCaptured = true;
                } else {
                    Assert.fail();
                }
                i++;
            }
        };
    }

    private void assertTest() throws InterruptedException {
        Thread.sleep(1000);

        Assert.assertTrue(eventCaptured);
        Assert.assertTrue(i == 1);
    }

    private void sendEvents(InputHandler inputHandlerStockQuoteStream) {
        //            inputHandlerStockQuoteStream.sendEvent(new EventImpl("StockQuoteStream", new Object[]{timestamp, symbol, price, volume}));
        inputHandlerStockQuoteStream.sendEvent(new EventImpl("StockQuoteStream", new Object[]{1, "IBM", 400, 1000}));
        inputHandlerStockQuoteStream.sendEvent(new EventImpl("StockQuoteStream", new Object[]{2, "IBM", 450, 500}));
        inputHandlerStockQuoteStream.sendEvent(new EventImpl("StockQuoteStream", new Object[]{3, "IBM", 470, 300}));
        inputHandlerStockQuoteStream.sendEvent(new EventImpl("StockQuoteStream", new Object[]{3, "IBM", 490, 300}));
        inputHandlerStockQuoteStream.sendEvent(new EventImpl("StockQuoteStream", new Object[]{4, "IBM", 100, 200}));
        inputHandlerStockQuoteStream.sendEvent(new EventImpl("StockQuoteStream", new Object[]{5, "IBM", 500, 200}));
    }


}