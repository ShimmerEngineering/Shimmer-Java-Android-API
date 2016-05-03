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
		
		System.out.println(LiteProtocolInstructionSet.InstructionsSet.getDescriptor().getValues());
		
		String[] enumValues = new String[LiteProtocolInstructionSet.InstructionsGet.getDescriptor().getValues().size()];
		int i =0;
		for (EnumValueDescriptor evd: LiteProtocolInstructionSet.InstructionsGet.getDescriptor().getValues()){
			enumValues[i] = evd.getName();
			i++;
		}
		System.out.println(enumValues);
		
		System.out.println(LiteProtocolInstructionSet.InstructionsSet.ACK_COMMAND_PROCESSED_VALUE);
		System.out.println(LiteProtocolInstructionSet.InstructionsSet.valueOf(255).name());
		String s = LiteProtocolInstructionSet.InstructionsSet.ACK_COMMAND_PROCESSED.toString();
		System.out.println(LiteProtocolInstructionSet.InstructionsSet.valueOf(s).getNumber());
		if (LiteProtocolInstructionSet.InstructionsSet.valueOf(254)!=null){
			System.out.println(LiteProtocolInstructionSet.InstructionsSet.valueOf(254).name());
		}
		
		System.out.println(LiteProtocolInstructionSet.InstructionsSet.ACK_COMMAND_PROCESSED.toString());
		
		System.out.println(LiteProtocolInstructionSet.InstructionsGet.GET_INFOMEM_COMMAND_VALUE);
		System.out.println(LiteProtocolInstructionSet.InstructionsGet.GET_INFOMEM_COMMAND.toString());
		
		System.out.println(LiteProtocolInstructionSet.InstructionsResponse.INFOMEM_RESPONSE_VALUE);
		System.out.println(LiteProtocolInstructionSet.InstructionsResponse.INFOMEM_RESPONSE.toString());
		LiteProtocolInstructionSet.InstructionsGet.GET_SAMPLING_RATE_COMMAND.getDescriptorForType().getValues();
		System.out.println(LiteProtocolInstructionSet.InstructionsGet.GET_BAUD_RATE_COMMAND.getValueDescriptor().getOptions());
		EnumValueOptions evd = LiteProtocolInstructionSet.InstructionsGet.GET_SAMPLING_RATE_COMMAND.getValueDescriptor().getOptions();
		
		Map<FieldDescriptor,Object> map = evd.getAllFields();
		System.out.println(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
		System.out.println(LiteProtocolInstructionSet.InstructionsGet.GET_SAMPLING_RATE_COMMAND.getValueDescriptor().getOptions());
		System.out.println(LiteProtocolInstructionSet.InstructionsGet.GET_SAMPLING_RATE_COMMAND.getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size")));
		int length = (int)LiteProtocolInstructionSet.InstructionsGet.GET_SAMPLING_RATE_COMMAND.getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
		System.out.println(length);
		int a = -114 & 0xff;
		length = (int)LiteProtocolInstructionSet.InstructionsGet.valueOf(-114&0xff).getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
		System.out.println(" " + length);
		length = (int)LiteProtocolInstructionSet.InstructionsResponse.INFOMEM_RESPONSE.getValueDescriptor().getOptions().getField(LiteProtocolInstructionSet.getDescriptor().findFieldByName("response_size"));
		System.out.println(" " + length);
		
	}

}

