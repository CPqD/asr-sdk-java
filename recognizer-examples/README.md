# Exemplos de uso

As aplicações de exemplo tem como objetivo demonstrar formas de uso da API Recognizer para realizar um reconhecimento de fala a partir de diferentes fontes de áudio.

Existem exemplos que utilizam três fontes de áudio:

+ **RecognizeSample**: realiza o reconhecimento da fala gravada em um arquivo de áudio. Demonstra o uso da API Recognizer e da fonte de áudio `FileAudioSource`, que lê o áudio de um arquivo WAV e envia para o servidor de reconhecimento de fala;
+ **BufferAudioSourceSample**: demonstra o uso da classe `BufferAudioSource` que permite que uma aplicação escreva *bytes* de áudio sem bloquear a *thread* principal. O envio do áudio para o servidor de reconhecimento de fala é feito por uma *thread* secundária;
+ **MicAudioSource**: demonstra o uso da classe `MicAudioSource` que realiza a captura de áudio a partir do microfone do sistema, utilizando a [API Java Sound](https://docs.oracle.com/javase/8/docs/api/javax/sound/sampled/package-summary.html). 

# Acesso ao Servidor CPqD ASR

Você deve possuir acesso a uma instância do servidor de reconhecimento de fala do CPqD. A instância pode estar instalada localmente ou acessível na nuvem. No último caso, é necessário possuir um usuário e credencial de acesso.

# Executando os exemplos

Execute o comando abaixo para compilar e empacotar os exemplos:

        mvn clean package

Para executar o exemplo `RecognizeSample`, execute o comando abaixo, ajustando a versão da biblioteca.

        java -cp target/recognizer-examples-2.0.2.jar br.com.cpqd.asr.recognizer.example.RecognizeSample <URL> <lmURI> <path-to-audio> [<user> <passwd>] 

