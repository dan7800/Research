/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.jms;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 04:56:06 -0500 (Tue, 14 Sep 2004) $
 */
public interface TopicConnection extends Connection {
    TopicSession createTopicSession(boolean transacted, int acknowledgeMode)
        throws JMSException;

    ConnectionConsumer createConnectionConsumer(
        Topic topic,
        String messageSelector,
        ServerSessionPool sessionPool,
        int maxMessages)
        throws JMSException;

    ConnectionConsumer createDurableConnectionConsumer(
        Topic topic,
        String subscriptionName,
        String messageSelector,
        ServerSessionPool sessionPool,
        int maxMessages)
        throws JMSException;
}
