# Exemplos de uso

As aplicações de exemplo tem como objetivo demonstrar formas de uso da API Recognizer para realizar um reconhecimento de fala a partir de diferentes fontes de áudio.

Existem exemplos que utilizam três fontes de áudio:

+ **FileRecognizer**: realiza o reconhecimento da fala gravada em um arquivo de áudio. Demonstra o uso da API Recognizer e da fonte de áudio `FileAudioSource`, que lê o áudio de um arquivo WAV e envia para o servidor de reconhecimento de fala;
+ **BufferRecognizer**: demonstra o uso da classe `BufferAudioSource` que permite que uma aplicação escreva *bytes* de áudio sem bloquear a *thread* principal. O envio do áudio para o servidor de reconhecimento de fala é feito por uma *thread* secundária;
+ **MicRecognizer**: demonstra o uso da classe `MicAudioSource` que realiza a captura de áudio a partir do microfone do sistema, utilizando a [API Java Sound](https://docs.oracle.com/javase/8/docs/api/javax/sound/sampled/package-summary.html).

# Acesso ao Servidor CPqD ASR

Você deve ter acesso a uma instância do servidor de reconhecimento de fala do CPqD. A instância ASR pode estar instalada localmente ou acessível na nuvem. 
No último caso, é necessário possuir um usuário e credencial de acesso.

# Executando os exemplos

Execute o comando abaixo, a partir da raiz do projeto, para compilar e empacotar a biblioteca e os exemplos:

```
mvn clean package
```

A partir do diretório `recognizer-examples`, execute o exemplo desejado. Por exemplo, para usar `FileRecognizer`, execute o comando abaixo, ajustando a versão da biblioteca se necessário (abaixo, foi usada a versão 2.0.5):

```
java -cp target/recognizer-examples-2.0.5.jar br.com.cpqd.asr.recognizer.example.FileRecognizer --server <URL> --lm <lmURI> --audio <path-to-audio>
```

O diretório `audio` oferece alguns arquivos de áudio que podem ser usados para teste.

Se você possui um servidor CPqD ASR local, com pacote de idioma para áudio de 8kHz e com modelo de fala livre, pode testar da seguinte forma, sem ter que passar usuário/senha de acesso:

```
java -cp target/recognizer-examples-2.0.5.jar br.com.cpqd.asr.recognizer.example.FileRecognizer --server ws://<ASR_IP>:8025/asr-server/asr --lm builtin:slm/general --audio audio/pt-br/87431_8k.wav
```

O campo <ASR_IP> deve ser substituído pelo IP do servidor CPqD ASR instalado.

Se você deseja realizar transcrição de um conjunto de arquivos de áudio no formato WAV, com um servidor CPqD ASR local, pode usar a classe `BatchRecognizer`:

```
java -cp target/recognizer-examples-2.0.5.jar br.com.cpqd.asr.recognizer.example.BatchRecognizer --server ws://<ASR_IP>:8025/asr-server/asr --lm builtin:slm/general --audio <diretório do áudio>
```

O campo <ASR_IP> deve ser substituído pelo IP do servidor CPqD ASR instalado.
