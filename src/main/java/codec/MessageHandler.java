package codec;

import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.Member;
import io.scalecube.net.Address;
import message.LeaderElectionRequestMsg;
import message.MessageParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author ashan on 2020-05-08
 */
public class MessageHandler {
    private List<FluxSink<Object>> emitters = new CopyOnWriteArrayList<>();
    private Cluster cluster;
    private long nodeID;
    private final Flux<Object> flux;

    public MessageHandler(Cluster cluster, long nodeID) {
        this.cluster = cluster;
        this.nodeID = nodeID;
        flux = Flux.create(emitter -> {
            emitters.add(emitter);
            emitter.onCancel(() -> removeEmitter(emitter));
            emitter.onDispose(() -> removeEmitter(emitter));
        });

    }

    public <T extends DynamicMsg> void sendReply(Address address, T message) {
        CompletableFuture.runAsync(() -> Flux.fromIterable(cluster.otherMembers().stream().filter(x -> x.address().equals(address))
                .collect(Collectors.toSet())).flatMap(
                member -> cluster.send(address, MessageParser.serialized(message))
        ).subscribe(null, Throwable::printStackTrace));
   }

    public <T extends DynamicMsg> void sendReplyToAll(T message) {
        Flux.fromIterable(cluster.otherMembers()).flatMap(
                member -> cluster.send(member.address(), MessageParser.serialized(message))
        ).subscribe(null, Throwable::printStackTrace);
    }

    public <T extends DynamicMsg> void sendMessage(Address address, T message) {
        CompletableFuture.runAsync(() -> cluster.send(address, MessageParser.serialized(message))
                .subscribe(null, Throwable::printStackTrace));
    }

    public <T extends DynamicMsg> void sendToAll(T message) {
        cluster.spreadGossip(MessageParser.serialized(message));
    }

    public void sendLeaderElectionRequest() {
        LeaderElectionRequestMsg requestMsg = new LeaderElectionRequestMsg(cluster.address(), cluster.member().alias());
        Collection<Member> memberSet = cluster.otherMembers().stream().filter(x -> Long.parseLong(x.alias().split("_")[1]) > nodeID).collect(Collectors.toSet());
        if (memberSet.size() > 0)
            Flux.fromIterable(memberSet).flatMap(
                    member -> {
                        System.out.println("Election Sending " + requestMsg.getMemberID() + " receiver : " + member.alias());
                        return cluster.send(member, MessageParser.serialized(requestMsg));
                    }
            ).subscribe(null, Throwable::printStackTrace);
        else
            System.out.println("No Seeds found for " + cluster.member().alias());
    }

    private void removeEmitter(FluxSink sink) {
        emitters.remove(sink);
    }
}
