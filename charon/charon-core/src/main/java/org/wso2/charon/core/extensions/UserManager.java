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
package org.wso2.charon.core.extensions;

import org.wso2.charon.core.attributes.Attribute;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.User;

import java.util.List;

/**
 * This is the interface for UserManager extension.
 * An implementation can plugin their own user manager-(either LDAP based, DB based etc)
 * by implementing this interface and mentioning it in configuration. 
 */
public interface UserManager extends Storage{

    /**
     * Obtains the user given the id.
     * @param userId
     * @return
     */
    public User getUser(String userId) throws CharonException;

    /**
     * Update the user in full.
     * @param user
     */
    public void updateUser(User user);

    /**
     * Update the user partially only with updated attributes.
     * @param updatedAttributes
     */

    public void updateUser(List<Attribute> updatedAttributes);

    /**
     * Delete the user given the user id.
     * @param userId
     */
    public void deleteUser(String userId);

    /**
     * Create user with the given user object.
     * @param user
     */
    public void createUser(User user);

}