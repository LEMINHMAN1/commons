/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.api.eventstream.query.utils;

import org.wso2.siddhi.api.eventstream.query.Query;

import java.util.List;

public class QueryHelper {

    public static Class[] generateAttributeClasses(List<String> outputPropertyList,
                                                   Query query) {

        Class[] classArray = new Class[outputPropertyList.size()];
        for (int i = 0; i < outputPropertyList.size(); i++) {

            String function = outputPropertyList.get(i).split("=")[1];

            if (function.contains("(")) {                   //Aggregator //avg(CSEStream.price)
                String[] funcArr = function.split("\\(");   //[0]=functionName, [1]=functionParameters

                //If the aggregator function is COUNT, then set the output class type as Long
                if ("COUNT".equals(funcArr[0].replaceAll(" ", "").toUpperCase())) {
                    classArray[i] = java.lang.Long.class;
                } else if ("".equals(funcArr[0].replaceAll(" ", "").toUpperCase())) { //for Default
                    String classTypeString = funcArr[1].split("\\)")[0].replace(" ", "").toUpperCase();
                    if (classTypeString.equals("INT")) {
                        classArray[i] = Integer.class;
                    } else if (classTypeString.equals("LONG")) {
                        classArray[i] = Long.class;
                    } else if (classTypeString.equals("FLOAT")) {
                        classArray[i] = Float.class;
                    } else if (classTypeString.equals("DOUBLE")) {
                        classArray[i] = Double.class;
                    } else {//    if(classTypeString.equals("STRING")){
                        classArray[i] = String.class;
                    }
                } else {    //For other aggregators
                    String[] property = funcArr[1].split("\\.");  //property[0]=StreamId,property[1]=Attribute)
                    classArray[i] = query.getInputEventStream(property[0]).getTypeForName(property[1].replace(")", "") //ex: price
                    );
                }
            } else {        //Without an aggregator
                String[] property = outputPropertyList.get(i).split("=")[1].split("\\.");  //property[0]=StreamId,property[1]=Attribute
                classArray[i] = query.getInputEventStream(property[0]).getTypeForName(property[1]
                );
            }
        }
        return classArray;

    }
}