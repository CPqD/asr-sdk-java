CPqD ASR Recognizer
===================

*O Recognizer é uma API para criação de aplicações de voz que utilizam o servidor CPqD ASR para reconhecimento de fala.*

Para maiores informações, consulte [a documentação do projeto](https://speechweb.cpqd.com.br/asr/docs).

### Códigos de Exemplo
Os códigos de exemplo estão sob o diretório [Recognizer Examples](https://github.com/CPqD/asr-sdk-java/tree/master/recognizer-examples)

### Construindo a partir do código-fonte

Baixe a última versão do repositório `git pull origin master` e execute `mvn clean install`.


### Releases

Para aqueles com permissão de gerar releases, o comando abaixo gera novo release no Github:

```
mvn release:prepare
```

Como ainda não há deploy dos artefatos gerados em repositório Maven, execute o comando seguinte para limpar dados gerados:

```
mvn release:clean
```


Licença
-------

Copyright (c) 2017 CPqD. Todos os direitos reservados.

Publicado sob a licença Apache Software 2.0, veja LICENSE.
