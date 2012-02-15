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
package org.wso2.siddhi.test.samples.statemachine.pattern.negation;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.api.QueryFactory;
import org.wso2.siddhi.api.eventstream.InputEventStream;
import org.wso2.siddhi.api.eventstream.query.Query;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.node.CallbackHandler;
import org.wso2.siddhi.core.node.InputHandler;
import org.wso2.siddhi.test.samples.statemachine.pattern.InfoStockPatternEG;
import org.wso2.siddhi.test.samples.statemachine.pattern.SimpleStockQuotePatternEG;

import static org.wso2.siddhi.api.condition.where.ConditionOperator.EQUAL;


public class NegationPattern4TestCase {

    private static final Logger log = Logger.getLogger(NegationPattern4TestCase.class);
    private volatile boolean eventCaptured;
    private volatile int i;

    @Before
    public void info() {
        log.debug("-----Query processed: Testing State-machine with nested negation-----");
        i = 0;
        eventCaptured = false;
    }

    @Test
    public void testAPI() throws Exception {
        //Instantiate SiddhiManager
        SiddhiManager siddhiManager = new SiddhiManager();
        QueryFactory qf = SiddhiManager.getQueryFactory();

        InputEventStream cseEventStream = new InputEventStream(
                "CSEStream",
                new String[]{"symbol", "price"},
                new Class[]{String.class, Integer.class}
        );

        InputEventStream infoStock = new InputEventStream(
                "infoStock",
                new String[]{"action", "timeStamp"},
                new Class[]{String.class, Long.class}
        );
        siddhiManager.addInputEventStream(cseEventStream);
        siddhiManager.addInputEventStream(infoStock);

        Query query = qf.createQuery(
                "StockQuote",
                qf.output("action=$0.action", "timeStamp=$0.timeStamp", "priceA=$1.price"),
                qf.inputStreams(qf.from(cseEventStream), qf.from(infoStock)),
                qf.pattern(
                        qf.every(
                                qf.condition("infoStock.action", EQUAL, "buy")  //0
                        ),
                        qf.nonOccurrence(qf.condition("CSEStream.price", EQUAL, "75"))
                                .followedBy(qf.nonOccurrence(qf.condition("infoStock.action", EQUAL, "cancel"))
                                                    .followedBy(qf.condition("CSEStream.price", EQUAL, "125"))) //1
                )
        );

        siddhiManager.addQuery(query);

        siddhiManager.addCallback(assignCallback());
        siddhiManager.update();


        InputHandler inputHandlerCseStream = siddhiManager.getInputHandler("CSEStream");
        InputHandler inputHandlerInfoStock = siddhiManager.getInputHandler("infoStock");
        sendEvents(inputHandlerCseStream, inputHandlerInfoStock);

        assertTest();
        siddhiManager.shutDownTask();
    }
    @Test
    public void testQuery() throws Exception {
        //Instantiate SiddhiManager
        SiddhiManager siddhiManager = new SiddhiManager();
        QueryFactory qf = SiddhiManager.getQueryFactory();
        siddhiManager.addQueries("CSEStream:= symbol[string], price [int]; \n" +
                                 "infoStock:= action[string], timeStamp[long]; \n" +
                                 "" +
                                 "StockQuote:=select action=$a1.action, timeStamp=$a1.timeStamp, priceA=$b3.price \n" +
                                 "from CSEStream, infoStock\n" +
                                 "pattern [a1=infoStock.action==\"buy\",\n" +
                                 "b1=CSEStream.price==75,\n" +
                                 "b2=infoStock.action==\"cancel\",\n" +
                                 "b3= CSEStream.price ==125  ]\n" +
                                 "every($a1) -> !$b1 -> !$b2-> $b3 ;");

        siddhiManager.addCallback(assignCallback());
        siddhiManager.update();

        InputHandler inputHandlerCseStream = siddhiManager.getInputHandler("CSEStream");
        InputHandler inputHandlerInfoStock = siddhiManager.getInputHandler("infoStock");
        sendEvents(inputHandlerCseStream, inputHandlerInfoStock);

        assertTest();
        siddhiManager.shutDownTask();
    }

    private void assertTest() throws InterruptedException {
        Thread.sleep(2000);
        Assert.assertTrue(eventCaptured);
        Assert.assertTrue(i == 1);
    }

    private void sendEvents(InputHandler inputHandlerCseStream,
                            InputHandler inputHandlerInfoStock) {
        SimpleStockQuotePatternEG eventGeneratorCseStream = new SimpleStockQuotePatternEG();
        InfoStockPatternEG eventGeneratorInfoStackPattern = new InfoStockPatternEG();

        inputHandlerInfoStock.sendEvent(eventGeneratorInfoStackPattern.generateEvent(0));
        inputHandlerInfoStock.sendEvent(eventGeneratorInfoStackPattern.generateEvent(1));
        inputHandlerCseStream.sendEvent(eventGeneratorCseStream.generateEvent(0, 125));
        inputHandlerInfoStock.sendEvent(eventGeneratorInfoStackPattern.generateEvent(0));
        inputHandlerCseStream.sendEvent(eventGeneratorCseStream.generateEvent(0, 105));
        inputHandlerCseStream.sendEvent(eventGeneratorCseStream.generateEvent(0, 125));
        inputHandlerCseStream.sendEvent(eventGeneratorCseStream.generateEvent(0, 105));
    }

    private CallbackHandler assignCallback() {
        return new CallbackHandler("StockQuote") {
            public void callBack(Event event) {
                log.debug("       Event captured  " + event);

                if ((Integer) event.getNthAttribute(2) == 125) {
                    eventCaptured = true;
                }
                i++;
            }
        };
    }


}