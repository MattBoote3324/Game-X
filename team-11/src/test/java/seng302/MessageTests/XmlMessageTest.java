package seng302.MessageTests;

import org.junit.Assert;
import org.junit.Test;
import seng302.Common.Messages.XmlMessage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class XmlMessageTest {

	@Test
	public void testXmlMessage(){
		byte subType = XmlMessage.RACE_SUBTYPE;

		InputStream inputStream = getClass().getResourceAsStream("/xml/TestRace.xml");
		String raceXmlText;
		try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
			raceXmlText = scanner.useDelimiter("\\A").next();
		}

		XmlMessage xmlMessage = new XmlMessage(subType, (short) raceXmlText.length(), raceXmlText);

		XmlMessage xmlMessageFromBytes = new XmlMessage(xmlMessage);

		Assert.assertEquals(subType, xmlMessageFromBytes.getXmlMsgSubType());
		Assert.assertEquals(raceXmlText, xmlMessageFromBytes.getXmlMessage());

	}
}