/*
 * Copyright (C) 2007 Yusuke Yamamoto
 * Copyright (C) 2011 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package twitter4j;

import java.util.Map;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.4
 */
interface ObjectFactory extends java.io.Serializable {


    Map<String, RateLimitStatus> createRateLimitStatuses(HttpResponse res) throws TwitterException;



    QueryResult createQueryResult(HttpResponse res, Query query) throws TwitterException;




    TwitterAPIConfiguration createTwitterAPIConfiguration(HttpResponse res) throws TwitterException;



    <T> ResponseList<T> createEmptyResponseList();

    OEmbed createOEmbed(HttpResponse res) throws TwitterException;
}
