package com.limou.aicodemother.ai;

import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGenTypeRoutingService {

    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routerCodeGenType(String userPrompt);
}
