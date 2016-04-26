package com.shimmerresearch.comms.radioProtocol;

import java.util.List;
import java.util.Map;

import com.google.protobuf.DescriptorProtos.EnumOptions;
import com.google.protobuf.DescriptorProtos.EnumValueOptions;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.shimmerresearch.comms.radioProtocol.ShimmerLiteProtocolInstructionSet.LiteProtocolInstructionSet;

public class testRadioProtocol {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/*
		System.out.println(LiteProtocolInstructionSet.Instructions.DATA_PACKET_VALUE);
		System.out.println(LiteProtocolInstructionSet.Instructions.DATA_PACKET.toString());
		System.out.println(LiteProtocolInstructionSet.Instructions.valueOf(LiteProtocolInstructionSet.Instructions.DATA_PACKET_VALUE).name());
*/
		System.out.println(LiteProtocolInstructionSet.Instructions.ACK_COMMAND_PROCESSED_VALUE);
		System.out.println(LiteProtocolInstructionSet.Instructions.valueOf(255).name());
		if (LiteProtocolInstructionSet.Instructions.valueOf(254)!=null){
			System.out.println(LiteProtocolInstructionSet.Instructions.valueOf(254).name());
		}
		
		System.out.println(LiteProtocolInstructionSet.Instructions.ACK_COMMAND_PROCESSED.toString());
		
		System.out.println(LiteProtocolInstructionSet.Instructions.GET_INFOMEM_COMMAND_VALUE);
		System.out.println(LiteProtocolInstructionSet.Instructions.GET_INFOMEM_COMMAND.toString());
		
		System.out.println(LiteProtocolInstructionSet.Instructions.INFOMEM_RESPONSE_VALUE);
		System.out.println(LiteProtocolInstructionSet.Instructions.INFOMEM_RESPONSE.toString());
		LiteProtocolInstructionSet.Instructions.GET_SAMPLING_RATE_COMMAND.getDescriptorForType().getValues();
		System.out.println(LiteProtocolInstructionSet.Instructions.GET_BAUD_RATE_COMMAND.getValueDescriptor().getOptions());
		EnumValueOptions evd = LiteProtocolInstructionSet.Instructions.GET_SAMPLING_RATE_COMMAND.getValueDescriptor().getOptions();
		
		Map<FieldDescriptor,Object> map = evd.getAllFields();
		System.out.println(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
		System.out.println(LiteProtocolInstructionSet.Instructions.GET_SAMPLING_RATE_COMMAND.getValueDescriptor().getOptions());
		System.out.println(LiteProtocolInstructionSet.Instructions.GET_SAMPLING_RATE_COMMAND.getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size")));
		int length = (int)LiteProtocolInstructionSet.Instructions.GET_SAMPLING_RATE_COMMAND.getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
		System.out.println(length);
		length = (int)LiteProtocolInstructionSet.Instructions.valueOf(3).getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
		System.out.println(" " + length);
		length = (int)LiteProtocolInstructionSet.Instructions.INFOMEM_RESPONSE.getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
		System.out.println(" " + length);
		
	}

}
