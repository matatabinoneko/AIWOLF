package com.carlo.aiwolf.lib;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.ContentBuilder;

public class AiWolfUtil {

	public static String GetTalkText(ContentBuilder builder){
		Content content=new Content(builder);
		return content.getText();
	}
}
