# Info Preliminari

## Perchè i Microservizi?
Tradizionalmente, i servizi era sviluppati come entità monolititche con tutti gli endpoints supporttati da un singolo servizio.
Una conseguenza di questo approccio è che molti servizi finiscono nella stesso server dove sono collocate funzionalità che non hanno nulla a che fare con i medesimi servizi.
Per esempio, il servizio che si occupa di fare il report su un prodotto non è necessariamente correlato agli endpoint di login o di fattura. Però finiscono tutti comunque sullo stesso server. Yet, they all reside in the same server.
Un problema molto ovvio on questo approccio è che un qualunque cambiamento del codice in una miniscola parte del sistema può avere effetti molto pesanti su altre parti del sistema. Inoltre, al crescere del progetto, diventa sempre più difficile tenere traccia di tutti i servizi e lo stato del loro codice.
Nel **Microservice-based architecture** però, invece di tenere tutti i moduli in un unico servizio monolitico, i servizi vengono progettati per risiedere su un apposito modulo (molto più piccolo del singolo modulo monolitico).
Per sempio, è possibile progettare un servizio di fatturazione che ridiede in un modulo ed un'altro servizio per il login che ridiere su un'altro modulo, entrambi indipendatnti l'un dall'altro. Quando un servizio necessita informazioni da un'altro servizio, basta fare una chiamata API al (micro)servizio d'interesse.

## Perchè Spring-boot and Spring-Cloud?
Spring Boot aiuta a create applicazioni Spring allievando il carico di lavoro, mentre Spring Cloud provvede allo sviluppatore un insieme di strumenti che rende più semplice la comunicazione tra microservizi.

## Sfide da affrontare con il Microservice-based Architecture
Anche se sviluppare un insieme di microservizi più piccolo può sembrare più facile, questo approccio presenta un insieme di problemi nativi al Microservice-based architecture:

1. **Componenti automtizzate**: diventa più difficile automatizzare il sistema perchè si hanno più componenti da gestire invece di un unico sistema monolitico, i.e. builds, deployment, monitoring, etc.
2. **Percepibilità**: Più componenti ci sono, più è importante è difficile tenere traccia, aggiornare e mantenere ogni componente.
3. **Configuration Management**: E' importante assicurarsi che ogni componenti sia stato aggiornato e ottimizzato per il suo ambiente.
4. **Debugging**: Diventa più difficile trovare bug ora che le componenti sono indipendenti lìuno dall'altro. Un sistema di loggng centralizzato e i Dashboards sono essenziali per gestire questo problema.
5. **Consistenza**: E' importante assicurarsi che i tools di sviluppo siano gli stessi tra componenti (i.e. non usare diversi linguaggi di programmazione od ambienti di lavoro per sviluppare egestire ogni componente. 

## Introduzione ai microservizi (ottimo per entrare nell'ottico dello sviluppo dei microserizi): https://www.youtube.com/watch?v=y8IQb4ofjDo&list=PLqq-6Pq4lTTZSKAFG6aCDVDP86Qx4lNas&index=1

_______________________________________________________________


# Azure Service Bus Queues, Topics e Subscriptions (Topics e Subscriptions non ci interessano per ora)

    Azure Service Bus supporta un insieme di tecnologie middleware cloud-based e message oriented tra cui il "reliable message queuing" e "durable publish/subscribe messaging".
    Un punto forza del Azure Service Bus è il "Decoupled Communication/Comunicazione Disacoppiata" che permette a servers e clients di connettersi ed eseguire le proprie operazioni in modo asincrono.
    Gli elementi alle base della messaggistica Azure sono:
    - Queues
    - Topics
    - Subscriptions

## Queues

    I Queues offrono una messagistica **FIFO** (First In, First Out) tra uno o più consumatori concorrenti. Cioè, i destinatari tipicamente ricevono e processano messaggi nell'ordine in cui arrivano nella Queue e solo un destinatario riceve e processa ogni messaggio.
    Un beneficio chiave nell'uso dei Queues è il "temporal decoupling" delle componenti dell'applicazione. In altre parole, i produttori (mandanti) e consumatori (destinatari) non devono mandare e riceve messaggi nello stesso momento percè il messaggi vengono depositati nei Queues.
    Inoltre i produttori non devono aspettare una risposta dai consumatori per poter ricevere o generare altri messaggi.


Documentazione:
- https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-queues-topics-subscriptions

________________________________________________


# Azure Bus Service - Inviare e Ricevere messaggi ad un Queue in modalità PeekLock

## Inviare Messaggi versa un Queue
Per inviare messaggi verso un Service Bus Queue è necessario istanziare e gestire nella propria applicazione un oggetto QueueClient ed inviare messaggi asincronomamente attraverso quest'ultimo.
Messaggi inviati da e ricevuti da un Service Bus Queue sono istanze della classe "Message".
Gli oggetti Message sono composti da:
- un insieme di proprietà standard (e.g. Label e TimeToLive),
- un dizionario che è usato per memorizzare proprietà specifiche all'applicazione,
- un body contenenti i dati dell'applicazione.

Un'applicazione può settare il body del messaggio passando al costuttore di Message un oggetto "serializable". In alternativa si può utilizzare un oggetto java.IO.InputStream.

  I Service Bus Queues supportano un messaggio di grandezza massimo uguale a 256K nello "Standard Tier" e fino ad un 1MB nel "Premium Tier".
  L'Header invece, il quale può includere sia le proprietà standard che custom dell'applicazione, ha una grandezza massima di 64KB.
  Non vi è limite al numero di messaggi che una Queue può contenere ma vi è un limite alla quantità toale di dati che una Queue può contenere (cioè 5GB, questo limite è definito nel momento di creazione della Queue).

## Ricevere Messaggi da un Queue
Il metodo principale per riceve messaggi da una Queue è di usare un oggetto ServiceBusContract.
I messaggi riceuto possoo essere gestiti in due modalità diverse:
- ReceiveAndDelete: quando un Service Bus riceve un Read Request per un messaggio contenuto nel Queue, il Service Bus marca il messaggio come "consumato", lo rimuove dalla Queue e lo ritorna all'applicazione. Questo approccio è il più semplice da implementare ed è meglio utilizzato nei contesto dove le applicazioni possono tollerare la perdità di messaggi in eventi di crash od altre interruzioni.
ReceiveAndDelete è la modalità di default per il consumo di messaggi.
- PeekLock: questa modalità è divisa in due fasi. Nella prima fase il Bus riceve una richiesta per consumare un messaggio nella Queue, cerca il messagio e lo mette in stato di "Lock" in modo da prevenire le altre applicazioni dal poterlo richiedere.
Nella seconda fase il Service Bus passa il messaggio all'applicazione, il quale consuma il messaggio e ritorna al Service Bus una richiesta di delete per liberare il Queue dal messaggio appena consumato.

Come recuperare dagli Errori:
Il Service Bus provvede anche delle funzioalità per recuperare/riprestinare i messaggi in caso di errori.
Se un'applicazione non è nella posizione di poter consumar eil messaggio richiesto, allora può invece invocare il metodo "unlockMessage" al posto di "deleteMessage" per riprestinare l'accesso del messaggio sul Queue (o per essere consumato dall'applicazione stessa o da altre applicazioni).
Inoltre ad ogni messaggio è associato un Timeout nell'evento in cui un'applicazione non consumi il messaggio in una data quntità di tempo.

Nell'evento in cui l'applicazioni vada in "crash" dopo aver processo i messaggi ma prima di aver potuto fare la richiesta di deltetMessage,allora il messaggio viene ri-inviato all'applicazione nel momento in cui viene riprestinato. Questo approccio è chiamato il "At Least Once Processing". Se invece, l'applicazione non è sviluppata per gestire la duplicazione di messaggi allora è necessario implementare delle funzionalità addizionali per gestire tale situazione.
Un esempio è l'uso del metodo "gtMessageId" sui messages, in quanto l'Id del message non varia tra tentativi di consegna.

## AutoForward (Optional, per ora)
The Auto-Forwarding feature enables you to chain a Topic Subscription or a Queue to destination Queue or Topic that is part of the same Service Bus namespace. When the feature is enabled, Service Bus automatically moves any messages arriving in the source Queue or Subscription into the destination Queue or Topic.

Auto-Forwarding allows for a range of powerful routing patterns inside Service Bus, including decoupling of send and receive locations, fan-in, fan-out, and application-defined partitioning.

The setup template creates the topology for this example as shown here. Note that the topic whose subscription auto-forwards into the target queue is made dependent on the target queue, so that the queue is created first. The connection between the two entitries is made with the forwardToproperty of the subscription pointing to the target queue.

## Duplicate Detection (Optional, per ora)
This sample illustrates the "duplicate detection" feature of Azure Service Bus.

The sample is specifically crafted to demonstrate the effect of duplicate detection when enabled on a queue or topic. The default setting is for duplicate detection to be turned off.

For setup instructions, please refer back to the main README file.
Enabling duplicate detection will keep track of the MessageId of all messages sent into a queue or topic during a defined time window.

If any new message is sent that carries a MessageId that has already been logged during the time window, the message will be reported as being accepted (the send operation succeeds), but the newly sent message will be instantly ignored and dropped. No other parts of the message are considered.

Read more about duplicate detection in the documentation.

Sample Code
The sample sends two messages that have the same MessageId and shows that only one of those messages is being enqueued and retrievable, if the queue has the duplicate-detection flag set.

The setup template creates the queue for this example by setting the requiresDuplicateDetection flag, which enables the feature, and it sets the duplicateDetectionHistoryTimeWindow to 10 minutes.

## Message Browsing (Optional, per ora)
This sample shows how to enumerate messages residing in a Queue or Topic subscription without locking and/or deleting them. This feature is typically used for diagnostic and troubleshooting purposes and/or for tooling built on top of Service Bus.

Read more about message browsing in the documentation.

Refer to the main README document for setup instructions.

Sample Code
The sample sends a set of messages into a queue and then enumerates them. When you run the sample repeatedly, you will see that messages accumulate in the log as we don't receive and remove them.

You will also observe that expired messages (we send with a 2 minute time-to-live setting) may hang around past their expiration time, because Service Bus lazily cleans up expired messages no longer available for regular retrieval.

The sample is documented inline in the MessageBrowse.java file.

## Prefetch (Optional, per ora)
When Prefetch is enabled in any of the official Service Bus clients, the receiver quietly acquires more messages, up to the PrefetchCount limit, beyond what the application initially asked for.

A single initial Receive or ReceiveAsync call therefore acquires a message for immediate consumption that is returned as soon as available. The client then acquires further messages in the background, to fill the prefetch buffer.
While messages are available in the prefetch buffer, any subsequent Receive/ReceiveAsync calls are immediately fulfilled from the buffer, and the buffer is replenished in the background as space becomes available. If there are no messages available for delivery, the receive operation empties the buffer and then waits or blocks, as expected.
With the ReceiveAndDelete receive mode, all messages that are acquired into the prefetch buffer are no longer available in the queue, and only reside in the in-memory prefetch buffer until they are received into the application through the Receive/ReceiveAsync or OnMessage/OnMessageAsync APIs. If the application terminates before the messages are received into the application, those messages are irrecoverably lost.

In the PeekLock receive mode, messages fetched into the Prefetch buffer are acquired into the buffer in a locked state, and have the timeout clock for the lock ticking. If the prefetch buffer is large, and processing takes so long that message locks expire while residing in the prefetch buffer or even while the application is processing the message, there might be some confusing events for the application to handle.

The application might acquire a message with an expired or imminently expiring lock. If so, the application might process the message, but then find that it cannot complete it due to a lock expiration. The application can check the LockedUntilUtc property (which is subject to clock skew between the broker and local machine clock). If the message lock has expired, the application must ignore the message; no API call on or with the message should be made. If the message is not expired but expiration is imminent, the lock can be renewed and extended by another default lock period by calling message.RenewLock().

If you need a high degree of reliability for message processing, and processing takes significant work and time, it is recommended that you use the prefetch feature conservatively, or not at all.

If you need high throughput and message processing is commonly cheap, prefetch yields significant throughput benefits.

## Scheduled Messages (Optional, per ora)
Sequencing and timestamping are two features that are always enabled on all Service Bus entities and surface through the SequenceNumber and EnqueuedTimeUtc properties of received or browsed messages.

For those cases in which absolute order of messages is significant and/or in which a consumer needs a trustworthy unique identifier for messages, the broker stamps messages with a gap-free, increasing sequence number relative to the queue or topic. For partitioned entities, the sequence number is issued relative to the partition.

The SequenceNumber value is a unique 64-bit integer assigned to a message as it is accepted and stored by the broker and functions as its internal identifier. For partitioned entities, the topmost 16 bits reflect the partition identifier. Sequence numbers roll over to zero when the 48/64-bit range is exhausted.

The sequence number can be trusted as a unique identifier since it is assigned by a central and neutral authority and not by clients. It also represents the true order of arrival, and is more precise than a time stamp as an order criterion.
The time-stamping capability acts as a neutral and trustworthy authority that accurately captures the UTC time of arrival of a message, reflected in the EnqueuedTimeUtc property. The value is useful if a business scenario depends on deadlines, such as whether a work item was submitted on a certain date before midnight, but the processing is far behind the queue backlog.

You can submit messages to a queue or topic for delayed processing; for example, to schedule a job to become available for processing by a system at a certain time.

Scheduled messages do not materialize in the queue until the defined enqueue time. Before that time, scheduled messages can be canceled. Cancellation deletes the message.

You can schedule messages either by setting the ScheduledEnqueueTimeUtc property when sending a message through the regular send path, or explicitly with the ScheduleMessageAsync API. The latter immediately returns the scheduled message's SequenceNumber, which you can later use to cancel the scheduled message if needed. Scheduled messages and their sequence numbers can also be discovered using "message browsing".

The SequenceNumber for a scheduled message is only valid while the message is in this state. As the message transitions to the active state, the message is appended to the queue as if had been enqueued at the current instant, which includes assigning a new SequenceNumber.

## Dead-Letter Queues (Optional, per ora)
All Service Bus Queues and Subscriptions have a secondary sub-queue, called the dead-letter queue (DLQ).

This sub-queue does not need to be explicitly created and cannot be deleted or otherwise managed independent of the main entity. The purpose of the Dead-Letter Queue (DLQ) is accept and hold messages that cannot be delivered to any receiver or messages that could not be processed.
Documentazione: https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-dead-letter-queues

## Partiotioned Queues (and Topics) (Optional, per ora)
Azure Service Bus employs multiple message brokers to process messages and multiple messaging stores to store messages. A conventional queue or topic is handled by a single message broker and stored in one messaging store. Service Bus partitions enable queues and topics, or messaging entities, to be partitioned across multiple message brokers and messaging stores. Partitioning means that the overall throughput of a partitioned entity is no longer limited by the performance of a single message broker or messaging store. In addition, a temporary outage of a messaging store does Snot render a partitioned queue or topic unavailable. Partitioned queues and topics can contain all advanced Service Bus features, such as support for transactions and sessions.


Documentazione: https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-queues
Documentazione: https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-partitioning

____________________________________________


# Esempio di un POM per poter usare Dipendenze per poter usare il Azure (Unified) SDK:

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-data-appconfiguration</artifactId>
  <version>1.0.0-preview.6/version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.0.0</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-security-keyvault-certificates</artifactId>
  <version>4.0.0-preview.5</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-security-keyvault-keys</artifactId>
  <version>4.0.0</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-security-keyvault-keys</artifactId>
  <version>4.0.0</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-messaging-eventhubs</artifactId>
  <version>5.0.0-preview.5</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-messaging-eventhubs-checkpointstore-blob</artifactId>
  <version>1.0.0-preview.3</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-blob</artifactId>
  <version>12.0.0</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-blob-batch</artifactId>
  <version>12.0.0</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-blob-cryptography</artifactId>
  <version>12.0.0</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-file-share</artifactId>
  <version>12.0.0-preview.5</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-queue</artifactId>
  <version>12.0.0</version>
</dependency>

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opencensus</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-cosmos</artifactId>
  <version>4.0.0-preview.1</version>
</dependency>

Documentazione: https://azure.github.io/azure-sdk/releases/2019-11/java.html


Name avg-poc-bff, usato dall'utente per interagire con un'applicazione(contiene entry point per mandare messaggi)

data factory: è in ascolto su un topic, quando arriva ad un messaggio indirizzato a data-factory1 (a lui), lo recepisce e risponde con un messaggio per il bff


___________________________________________________


# Esempio di un POM per poter usare Dipendenze per poter implementare il Azure Service Bus:

<project 
    xmlns="http://maven.apache.org/POM/4.0.0" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.1.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>queuesgettingstarted</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>QueuesGettingStarted</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>1.8</java.version>
        <azure.version>2.2.0</azure.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>com.microsoft.azure</groupId>
                <artifactId>azure-webapp-maven-plugin</artifactId>
                <version>1.8.0</version>
                <configuration>
                    <schemaVersion>V2</schemaVersion>
                    <!-- Reference <serverId> in Maven's settings.xml to authenticate with Azure -->
                    <authentication>
                        <serverId>2919748c-d9f9-42e7-a0b8-55cbeb2cee2d</serverId>
                    </authentication>

                    <!-- Web App information -->
                    <resourceGroup>Avantgarde-rg</resourceGroup>
                    <appName>avg-poc-bff</appName>
                    <region>westeurope</region>
                    <pricingTier>P1V2</pricingTier>

                    <!-- Java Runtime Stack for Web App on Windows-->
                    <runtime>
                        <os>Windows</os>
                        <javaVersion>1.8</javaVersion>
                        <webContainer>tomcat 8.5</webContainer>
                    </runtime>
                    <!-- Deployment settings -->
                    <deployment>
                        <resources>
                            <resource>
                                <directory>${project.basedir}/target</directory>
                                <targetPath>${project.build.finalName}</targetPath>
                                <includes>
                                    <include>*.war</include>
                                </includes>
                            </resource>
                        </resources>
                    </deployment>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.6.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <debug>true</debug>
                    <debuglevel>lines,vars,source</debuglevel>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                    <archive>
                        <manifest>
                            <mainClass>com.microsoft.azure.servicebus.samples.queuesgettingstarted.QueuesGettingStarted</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>


    </build>


    <dependencies>

        <dependency>
            <groupId>com.microsoft.azure</groupId>
            <artifactId>azure-active-directory-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.microsoft.azure</groupId>
            <artifactId>azure-keyvault-secrets-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.microsoft.azure</groupId>
            <artifactId>azure-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.microsoft.azure</groupId>
            <artifactId>azure-storage-spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.microsoft.azure</groupId>
            <artifactId>azure-servicebus-jms-spring-boot-starter</artifactId>
            <version>2.1.7</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>mssql-jdbc</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.microsoft.azure</groupId>
            <artifactId>azure-servicebus</artifactId>
            <version>1.2.8</version>
        </dependency>
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>[1.2.17,]</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>[1.7.25,]</version>
        </dependency>
        <dependency>
            <groupId>commons-cli</groupId>
            <artifactId>commons-cli</artifactId>
            <version>[1.4,]</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>[2.8.2,]</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.microsoft.azure</groupId>
                <artifactId>azure-spring-boot-bom</artifactId>
                <version>${azure.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>


</project>

___________________________________________________


**Codice da usare per "bypass"-are il login di sicurezza di Spring**:
    package com.example.AzureDemo;

    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

    @Configuration
    public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity.authorizeRequests().antMatchers("/").permitAll();
        }

    }

_____________________________________________________


# Azure Blob Storage

## Introduzione
Azure Blob Storage è un servizio ottimizzato per l'immagazzinamento di grandi quantità di dati non strutturato/disorganizzati.
I dati non struttuai sono dati che non aderiscono ad uno specifico Data Model (o definizione), come ad esempio i file di testo o i binary data.

  Azure Blog Storage è diviso in 4 fasce utente:
  - Premium: usato per immagazzinare dati performance-sensitive,
  - Hot: per i dati che vengono acceduti frequentemente,
  - Cold:per i dati che vengono acceduti di rado,
  - Archived: per i dati che non vengono quasi mai acceduti.


Utilizzi comuni di Azure Blob storage:
- servire images o documenti direttamente ad un browser,
- immagazzinare file per utilizzo tra tanti dispositivi remoti,
- steaming di video e audio,
- stesura di files di log,
- immagazzinare file e dati di backup (a scopo di riprestino od archiviazione)
- immagazzinare file a scopo di analisi.

Gli utenti/applicazioni Client possono accedere a Blog Storage tramite richieste HTTP/HTTPS. Gli oggetti in Blog Storage possono essere accevuti via Azure Storage REST API, Azure Powershell, Azure CLI, oppure tramite l'uso di un'applicazione sviluppata tramite il del Azure Storage Client Library (il quale supporta i linguaggi .NET, Java, Node.js, Python, Go, PHP, and Ruby).

____________________________________________________________


## Cosa sono i Block blobs, append blobs, e page blobs?

Il Azure Blog Storage metter a disposizione 3 tipi di risorse:
- **Storage Account**: offre un namespace unico per immagazinare dati. Tutti gli oggetti salvati all'interno dell'Azure Storage ha un indirizzo che include il namespace del Storage Account (cioè indirizzo finale = account name + Azure Storage blob endpoint),
E.g. http://mystorageaccount.blob.core.windows.net 
- **Un (insieme di) Container(s)** (all'interno dello Storage Account): organizza un insieme di Blobs (simile ad un directory in un file system). Un Container può contenere un numero infinito di Blobs,
- **Un (insieme di) Blob(s)** (all'interno dello Storage Account): Azure Blob Storage supporta 3 tipi di Blobs:
  - **Block Blobs**: Blobs costituiti da blocchi di dati (tipicamente testi o binary data) che possono essere gestiti individualmente. Un singolo Block Blob può contenere fino a 4.7TB di dati.
  - **Append Blobs**: simili ai Block Blobs ma ottimizzati per le operazioni di append.
  - **Page Blobs**: usati per immagazzinare Random Access Files (fino ad un limite di 8TB) che servono per i Virtual Hard Drives (VHD). 

![Image description](blob1.png)

## Block Blobs
I Block blobs permettono di caricare file enormi efficientemente. Sono composti di blocchi, ognuno dei quali è identificato senza un proprio ID block. Un Block Blob è create o modificato scrivendo un insieme di blocchi e "committandoli" tramite i loro rispettivi Block ID. Ogni blocco ha una grandezza diversa fio ad un massimo di 100MB ed ogni Block Blob può contenere fino a 50000 blocchi.
Quando viene caricato un blocco in un block, non ne divent parte finchè non viene "committato" e aquiscisce un nouvo block ID
Se viene scritto un Block per une Blob che non esiste, allora un nuovo Block Blob viene creato con grandezza pari a zero bytes. Questo Block Blob però viene inserito nella lista di Blobs non committati. Se dopo una settimana il Blob rimane vuoto, senza Blocks committati al suo interno, il Blob verrà automaticamente buttato via.

## Append Blobs
Un Append Blob è costituito da una serie di Blocks ed è ottimizzato per le operazioni di append. Quando viene modificato un Append Blob, i blocchi sono aggiunti all fine del Blob (non è possibile inseririli da qualunque altra parte). Aggiornare o cancellare Blocks esistenti non è possibile e, a differenza dei Block Blobs, un Appnd Blob non espone gli ID dei suoi Blocks.

## Page Blobs
I Page Blobs sono una collezione di pagine (ciascuna a 512 bytes) ottiizzate per le operazioni di Random Read e Write.
Per generare un Page Blob è necessario inizializzare un Oage Blob e specificare la grandezza massima che il Page Blob può raggiungere. Per aggiungere od aggiornare i contenuti del Page Blob è necessario scrivere una o più pagine specificando l'offset e il range per allinearsi con i 512-byte page boundaries. Un comando di write su un Page Blob può sovrascrivere una o più pagine fino ad un massimo di 4MB (tutto in una volta). Le operazioni di write sono immediatamente committate sul Page Blob.

_______________________________________________________________


## Come spostare dati nel Blob Storage:
- AzCopy: un tool per il CMD Windows e Linux che permette di copiare dati ed inoltrarli direttamente (o anche per richiederli) al Blob Store.
- La liberia Azure Storage Data Movement (usato dal .NET).
- Azure Data Factory.
- Blobfuse (supportato da Linux).
- Il Azure Data Box Service.
- Il Azure Import/Export Service.

Documentazione:
https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction

______________________________________________________


## Come creare un Storage Account:
https://docs.microsoft.com/en-us/azure/storage/common/storage-quickstart-create-account?toc=%2Fazure%2Fstorage%2Fblobs%2Ftoc.json&tabs=azure-portal

____________________________________________________________________


## Usare un Azure Storage Emulator a scopo di sviluppo e Testing
Il Microsoft Azure Storage Emulator è un tool usato per emulare il servizio di Azure Storage Blob e Queue per fini di sviluppo codice a livello locale (evitando di dover creare un Azure Subscirption).
L'emulatore usa un Local Microsoft SQL Server 2012 Express LocalDB per emulare i servizi oferti da Azure Blob Storage (è anche possibile configurare l'emulatore per far girare un Server SQL al posto del LocalDB).
Ogni richiesta fatta verso l'emulatore deve essere autorizzata (a meno che non sia una richiesta anonima). Le richieste possono essere autorizzare usando il Shared Key authentication o con un Shared Access Signature (SAS).
In questo caso si fa uso di una chiave di autenticazione predefinita per l'emulatore:

  **Account name: devstoreaccount1**
  **Account key:** **Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==**

Documentazione: https://docs.microsoft.com/en-us/azure/storage/common/storage-use-emulator#get-the-storage-emulator
Documentazione: https://medium.com/oneforall-undergrad-software-engineering/setting-up-the-azure-storage-emulator-environment-on-windows-5f20d07d3a04

__________________________________________________________


# Esempio di un POM per poter usare Dipendenze per poter implementare il Azure Blob Storage:

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.2.2.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.example</groupId>
	<artifactId>AzureBusNBlobDemo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>AzureBusNBlobDemo</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>1.8</java.version>
		<azure.version>2.2.0</azure.version>
	</properties>

	<dependencies>
	 <!-- When using the "Azure Storage" dependancy DO NOT include in the POM the
 	 "azure-keyvault-secrets-spring-boot-starter" or "azure-storage-spring-boot-starter" dependancy, they will
 	  cause the program do create duplicate jars which will conflict with one another
 	  during runtime (this issue is not detectable at compile time) -->
	 <!-- Generated error message:
 	 Exception in thread "main" java.lang.SecurityException: class "com.microsoft.azure.storage.blob.BlobListingDetails"'s signer
 	 information does not match signer information of other classes in the same package. -->

		<dependency>
			<groupId>com.microsoft.azure</groupId>
			<artifactId>azure-active-directory-spring-boot-starter</artifactId>
		</dependency>
		<dependency>
			<groupId>com.microsoft.azure</groupId>
			<artifactId>azure-keyvault-secrets-spring-boot-starter</artifactId>
		</dependency>
		<dependency>
			<groupId>com.microsoft.azure</groupId>
			<artifactId>azure-spring-boot-starter</artifactId>
		</dependency>
<!--	<dependency>-->
<!--		<groupId>com.microsoft.azure</groupId>-->
<!--		<artifactId>azure-storage-spring-boot-starter</artifactId>-->
<!--	</dependency>-->
		<dependency>
			<groupId>com.microsoft.azure</groupId>
			<artifactId>azure-storage</artifactId>
			<version>8.4.0</version>
		</dependency>
		<dependency>
			<groupId>com.microsoft.sqlserver</groupId>
			<artifactId>mssql-jdbc</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
			<exclusions>
				<exclusion>
					<groupId>org.junit.vintage</groupId>
					<artifactId>junit-vintage-engine</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
	</dependencies>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>com.microsoft.azure</groupId>
				<artifactId>azure-spring-boot-bom</artifactId>
				<version>${azure.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>

__________________________________________________________


# Glossario:
**Service-Oriented Architecture**: you're bulding a service for re-useability, you're not focused on building a service for a specific context.

**"Serializable"**: la serializzazione in Java è un meccanismo nel quale lo stato di un oggetto è scirtto in formato "byte-stream". E' maggiormente utilizzato per le tecnologies Hibernate, RMI, JPA, EJB and JMS.
L'operazione inversa (byte-stream -> oggetto) si chiama "De-serialilzzazione". Il concetto di Serializzazione-Deserializzazione è platform-independant, cioè si può serializzare un oggetta in una piattaforma e deserializzarla in una piattaforma completamente diversa (ed essere sicuri che sia compatibile per quel sistema).
Per serializzare un oggetto, si chiama "writeObect()" della class ObjectOutputStream mentre per deserializzarlo si usa il metodo "readObject()" della class ObjectInputStream.
Documentazione: https://www.javatpoint.com/serialization-in-java


**MappingJackson2MessageConverter**: Message converter that uses Jackson 2.x to convert messages to and from JSON. Maps an object to a BytesMessage, or to a TextMessage if the targetType is set to MessageType.TEXT. Converts from a TextMessage or BytesMessage to an object.



**serialVersionUID**: The serialization runtime associates with each serializable class a version number, called a serialVersionUID, which is used during deserialization to verify that the sender and receiver of a serialized object have loaded classes for that object that are compatible with respect to serialization. If the receiver has loaded a class for the object that has a different serialVersionUID than that of the corresponding sender's class, then deserialization will result in an InvalidClassException. A serializable class can declare its own serialVersionUID explicitly by declaring a field named serialVersionUID that must be static, final, and of type long:

ANY-ACCESS-MODIFIER static final long serialVersionUID = 42L;
If a serializable class does not explicitly declare a serialVersionUID, then the serialization runtime will calculate a default serialVersionUID value for that class based on various aspects of the class, as described in the Java(TM) Object Serialization Specification. However, it is strongly recommended that all serializable classes explicitly declare serialVersionUID values, since the default serialVersionUID computation is highly sensitive to class details that may vary depending on compiler implementations, and can thus result in unexpected InvalidClassExceptions during deserialization. Therefore, to guarantee a consistent serialVersionUID value across different java compiler implementations, a serializable class must declare an explicit serialVersionUID value. It is also strongly advised that explicit serialVersionUID declarations use the private modifier where possible, since such declarations apply only to the immediately declaring class serialVersionUID fields are not useful as inherited members.

**JmsListener**: usato per costruire applicazioni capaci di rimanere in attesa ed ascolto per un messaggio.
JmsTemplate: usato per inviare messaggi ad una destinazione.
L'annotazione @jmsListener permette di dichiarare una funzione come "listener" che rimane in attesa di input da un mittente specifico.
Documentazione allegata: https://spring.io/guides/gs/messaging-jms/

**Logger**: usato per stampare stringhe su console, ogni logger viene dichiarato e bindato ad una specifica classe.
Documentazione allegata: https://www.geeksforgeeks.org/logger-getlogger-method-in-java-with-examples/

**Cross-origin resource sharing (CORS)**: un meccanismo che permette la richista di risorse riservate su una pagina web da parte di un'altro dominio collocato fuori dal ominion della pagina web su cui la risorsa è conservata.

_________________________________________________________________