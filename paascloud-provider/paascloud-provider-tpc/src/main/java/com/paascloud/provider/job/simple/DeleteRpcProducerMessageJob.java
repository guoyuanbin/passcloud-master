package com.paascloud.provider.job.simple;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.paascloud.elastic.lite.annotation.ElasticJobConfig;
import com.paascloud.provider.model.dto.TpcMqMessageDto;
import com.paascloud.provider.service.TpcMqMessageService;
import com.passcloud.common.base.constant.AliyunMqTopicConstants;
import com.passcloud.common.conf.properties.PaascloudProperties;
import com.passcloud.common.zk.generator.UniqueIdGenerator;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 定时清理所有生产者发送成功的消息数据.
 *
 * @author paascloud.net @gmail.com
 */
@Slf4j
@ElasticJobConfig(cron = "0 0 1 1/1 * ?")
public class DeleteRpcProducerMessageJob implements SimpleJob {

	@Resource
	private PaascloudProperties paascloudProperties;
	@Resource
	private TpcMqMessageService tpcMqMessageService;

	/**
	 * Execute.
	 *
	 * @param shardingContext the sharding context
	 */
	@Override
	public void execute(final ShardingContext shardingContext) {

		final TpcMqMessageDto message = new TpcMqMessageDto();
		message.setMessageBody(JSON.toJSONString(shardingContext));
		message.setMessageTag(AliyunMqTopicConstants.MqTagEnum.DELETE_PRODUCER_MESSAGE.getTag());
		message.setMessageTopic(AliyunMqTopicConstants.MqTopicEnum.TPC_TOPIC.getTopic());
		message.setProducerGroup(paascloudProperties.getAliyun().getRocketMq().getProducerGroup());
		String refNo = Long.toString(UniqueIdGenerator.generateId());
		message.setRefNo(refNo);
		message.setMessageKey(refNo);
		tpcMqMessageService.saveAndSendMessage(message);
	}
}
