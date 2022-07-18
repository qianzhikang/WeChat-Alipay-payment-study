package com.example.paymentstudy.enums.wxpay;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author qianzhikang
 */

@AllArgsConstructor
@Getter
public enum WxNotifyType {

	/**
	 * 支付通知
	 */
	NATIVE_NOTIFY("/api/wx-pay/native/notify"),


	/**
	 * 退款结果通知
	 */
	REFUND_NOTIFY("/api/wx-pay/refunds/notify");

	/**
	 * 类型
	 */
	private final String type;
}
