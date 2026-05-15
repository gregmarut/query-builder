/*
 * SkyBit Labs CONFIDENTIAL
 *  ---------------------------
 *  Copyright (c) 2024 SkyBit Labs LLC.
 *  All rights reserved.
 *
 *  NOTICE: All information contained herein is, and remains the property of SkyBit Labs LLC
 *  and its suppliers if any. The intellectual and technical concepts contained herein are
 *  proprietary to SkyBit Labs LLC and its suppliers and may be covered by U.S. and
 *  Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material is strictly forbidden
 *  unless prior written permission is obtained from SkyBit Labs LLC.
 *
 *  Contributors:
 *      Greg Marut
 */

package com.gregmarut.querybuilder.cypher;

public class CypherConstants
{
	//cypher identifiers must start with a letter or underscore and may contain letters, digits, and underscores thereafter
	public static final String IDENTIFIER_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*$";
	
	public static final String FLAG_ID_PROPERTY_ONLY = "id_prop_only";
}
