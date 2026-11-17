# Serpent Wand — Fabric 1.21.11

Projeto-fonte para Minecraft 1.21.11, Java 21.

## Como usar
- Segure o botão de uso (clique direito no PC ou toque e segure no controle de toque).
- O jogador mantém o braço levantado por cerca de 1,2 segundo enquanto prepara a magia.
- Solte o botão para disparar o efeito visual de partículas verdes e a aura circular.

## Recursos
- Item próprio `serpent:serpent_wand`.
- Mantém o modelo 3D e a textura enviados no JAR original.
- Pose de preparação com o braço levantado (`UseAction.BOW`).
- Pulso de partículas verdes e rastro decorativo ao soltar o botão.
- Efeito estritamente visual: não cria projétil físico, não colide, não causa dano e não aplica efeitos negativos.

## Compilar
Requer computador com Java 21 e Gradle instalado, com internet para baixar as dependências do Fabric.
Na pasta do projeto, execute `gradle build`.
O JAR será gerado em `build/libs/serpent-wand-1.0.0.jar`.

Instale o JAR compilado na pasta `mods` do perfil Fabric 1.21.11 junto com Fabric API.

Este ambiente não dispõe de Gradle/dependências do Minecraft para compilar ou testar o JAR binário; o arquivo entregue é o projeto-fonte.

## Compilar o .jar

Requer JDK 21 e acesso à internet (Fabric Maven + Mojang).

    ./gradlew build        # Linux/macOS
    gradlew.bat build      # Windows

O jar final fica em `build/libs/serpent-wand-1.0.0.jar` (ignore o `-sources.jar`).
Coloque-o na pasta `mods` junto com o Fabric API 0.141.6+1.21.11.
