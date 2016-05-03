package com.yongjian.sakurago.utils;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.text.method.NumberKeyListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Activity;
import android.widget.TextView;
import android.widget.EditText;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.telephony.TelephonyManager;
import java.util.UUID;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class CharsetDetect {
	private final static String DEBUG_TAG			= "CharsetDetect";

	private static boolean isBufferAscii(byte []buffer, int length) {
		for(int i = 0; i < length - 1; i++) {
			if(buffer[i] > 0 && buffer[i] < 127) {
			} else {
				return false;
			}
		}
		return true;
	}

	private static boolean __is_gbk_char(int byte0, int byte1) {
		// 信息交换用汉字编码字符
		if(byte0 >= 0x81 && byte0 <= 0xFE && byte1 >= 0x40 && byte1 <= 0xFE)
			return true;

		return false;
	}

	private static boolean __is_gb2312_char(int byte0, int byte1) {
		// 信息交换用汉字编码字符
		if(byte0 >= 0xA1 && byte0 <= 0xF7 && byte1 >= 0xA1 && byte1 <= 0xFE)
			return true;

		return false;
	}

	/*
	 * UTF-8编码规则
	 * Bits  First        Last          Bytes    Byte 1      Byte 2      Byte 3      Byte 4      Byte 5      Byte 6
	 * 7     U+0000       U+007F        1        0xxxxxxx
	 * 11    U+0080       U+07FF        2        110xxxxx    10xxxxxx
	 * 16    U+0800       U+FFFF        3        1110xxxx    10xxxxxx    10xxxxxx
	 * 21    U+10000      U+1FFFFF      4        11110xxx    10xxxxxx    10xxxxxx    10xxxxxx
	 * 26    U+200000     U+3FFFFFF     5        111110xx    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx
	 * 31    U+4000000    U+7FFFFFFF    6        1111110x    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx    10xxxxxx
	 */
	private static boolean __isBufferUTF8(byte []buffer, int length) {
		if (null == buffer || length <= 0) {
			return false;
		}

		boolean	requireByte	= false;				// 是否有后续字节
		int	requireByteNum	= 0;					// 后续字节个数

		for (int i = 0; i < length; i++) {
			byte current = buffer[i];

			if (requireByte) {					// 有byte2 ~ byte6
				if ((current & 0xC0) == 0x80) {			// 当前以0x10开头，判断是否满足utf8编码规则
					requireByteNum--;
					if (requireByteNum == 0)
						requireByte = false;
				} else {
					return false;
				}
			}

			if ((current & 0x80) == 0x00) {				//当前字节小于128，标准ASCII码范围
				continue;
			}else if ((current & 0xE0) == 0xC0) {			//当前以0x110开头，标记2字节编码开始，后面需紧跟1个0x10开头字节
				requireByte = true;
				requireByteNum = 1;
				continue;
			}else if ((current & 0xF0) == 0xE0) {			//当前以0x1110开头，标记3字节编码开始，后面需紧跟2个0x10开头字节
				requireByte = true;
				requireByteNum = 2;
				continue;
			}else if ((current & 0xF8) == 0xF0) {			//当前以0x11110开头，标记4字节编码开始，后面需紧跟3个0x10开头字节
				requireByte = true;
				requireByteNum = 3;
				continue;
			}else if ((current & 0xFC) == 0xF8) {			//当前以0x111110开头，标记5字节编码开始，后面需紧跟4个0x10开头字节
				requireByte = true;
				requireByteNum = 4;
				continue;
			}else if ((current & 0xFE) == 0xFC) {			//当前以0x1111110开头，标记6字节编码开始，后面需紧跟5个0x10开头字节
				requireByte = true;
				requireByteNum = 5;
				continue;
			}
		}

		if(requireByte || 0 != requireByteNum)
			return false;

		return true;
	}
}

