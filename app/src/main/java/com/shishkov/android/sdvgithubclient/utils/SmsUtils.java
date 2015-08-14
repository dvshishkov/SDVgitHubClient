package com.shishkov.android.sdvgithubclient.utils;

import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsUtils {

	public static SmsMessage getSmsFromBundle(Bundle bundle) {
		try {
			if (bundle != null) {
				final Object[] pdus = (Object[]) bundle.get("pdus");
				for (Object pdu : pdus) {
					return SmsMessage.createFromPdu((byte[]) pdu);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getSmsBody(SmsMessage sms) {
		return sms.getDisplayMessageBody().toLowerCase();
	}

}
