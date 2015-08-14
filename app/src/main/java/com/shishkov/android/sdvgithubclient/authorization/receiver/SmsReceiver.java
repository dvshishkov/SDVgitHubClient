package com.shishkov.android.sdvgithubclient.authorization.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;

import com.shishkov.android.sdvgithubclient.authorization.controller.Activity.TwoFactorAuthActivity;
import com.shishkov.android.sdvgithubclient.utils.SmsUtils;

public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
			final Bundle bundle = intent.getExtras();
			if (bundle != null) {

				try {
					SmsMessage sms = SmsUtils.getSmsFromBundle(bundle);
					String smsBody = SmsUtils.getSmsBody(sms);
					String numbersInSms = smsBody.replaceAll("[^-?0-9]+", " ");
					String[] numbers = numbersInSms.trim().split(" ");
					for (String number : numbers) {
						if (number.length() == TwoFactorAuthActivity.GIT_HUB_OTP_CODE_LENGTH) {
							Intent i = new Intent(TwoFactorAuthActivity.OTP_CODE_RECEIVED);
							i.putExtra(TwoFactorAuthActivity.EXTRA_OTP_CODE, number);
							LocalBroadcastManager.getInstance(context).sendBroadcast(i);
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
