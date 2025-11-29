package com.limou.aicodemother.ai.core;

import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiCodeGeneratorFacadeTest {
@Resource
private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个留言板,不超过20行代码", CodeGenTypeEnum.HTML,1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个留言板,不超过20行代码", CodeGenTypeEnum.HTML,1L);
        List<String> block = stringFlux.collectList().block();
        String join = String.join("", block);
        System.out.println( join);
        Assertions.assertNotNull(stringFlux);
    }


}