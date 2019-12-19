# Example of Sleuth integration to Spring Boot Services


### Goal0: Example chart usage
This is an example how to use a library chart with Istio


### Goal1: Log propagation
```
Controller                 : Header Name: accept Header Value: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
2019-12-19 14:20:02.265  INFO [frontend,cfb70c32157b22f1,cfb70c32157b22f1,false] 21133 --- [nio-8080-exec-1] frontend.HelloController                 : Header Name: sec-fetch-site Header Value: none
2019-12-19 14:20:02.279  INFO [frontend,cfb70c32157b22f1,cfb70c32157b22f1,false] 21133 --- [nio-8080-exec-1] frontend.HelloController                 : Header Name: sec-fetch-mode Header Value: navigate
2019-12-19 14:20:02.280  INFO [frontend,cfb70c32157b22f1,cfb70c32157b22f1,false] 21133 --- [nio-8080-exec-1] frontend.HelloController                 : Header Name: accept-encoding Header Value: gzip, deflate, br
2019-12-19 14:20:02.280  INFO [frontend,cfb70c32157b22f1,cfb70c32157b22f1,false] 21133 --- [nio-8080-exec-1] frontend.HelloController                 : Header Name: accept-language Header Value: en-US,en;q=0.9,ru;q=0.8
```

### Goal2: B3 traces propagation

The most common propagation use case is to copy a trace context from a client 
sending an RPC request to a server receiving it.

In this case, the same span ID is used, which means that both the client and
server side of an operation end up in the same node in the trace tree.

Here's an example flow using multiple header encoding, assuming an HTTP request carries the propagated trace:

```
   Client Tracer                                                  Server Tracer     
┌───────────────────────┐                                       ┌───────────────────────┐
│                       │                                       │                       │
│   TraceContext        │          Http Request Headers         │   TraceContext        │
│ ┌───────────────────┐ │         ┌───────────────────┐         │ ┌───────────────────┐ │
│ │ TraceId           │ │         │ X-B3-TraceId      │         │ │ TraceId           │ │
│ │                   │ │         │                   │         │ │                   │ │
│ │ ParentSpanId      │ │ Inject  │ X-B3-ParentSpanId │ Extract │ │ ParentSpanId      │ │
│ │                   ├─┼────────>│                   ├─────────┼>│                   │ │
│ │ SpanId            │ │         │ X-B3-SpanId       │         │ │ SpanId            │ │
│ │                   │ │         │                   │         │ │                   │ │
│ │ Sampling decision │ │         │ X-B3-Sampled      │         │ │ Sampling decision │ │
│ └───────────────────┘ │         └───────────────────┘         │ └───────────────────┘ │
│                       │                                       │                       │
└───────────────────────┘                                       └───────────────────────┘
```
### Goal3: /actuator/prometheus endpoint

```
jvm_classes_loaded_classes 8357.0
# HELP jvm_threads_peak_threads The peak live thread count since the Java virtual machine started or peak was reset
# TYPE jvm_threads_peak_threads gauge
jvm_threads_peak_threads 22.0
# HELP jvm_threads_states_threads The current number of threads having NEW state
# TYPE jvm_threads_states_threads gauge
jvm_threads_states_threads{state="runnable",} 7.0
jvm_threads_states_threads{state="blocked",} 0.0
jvm_threads_states_threads{state="waiting",} 12.0
jvm_threads_states_threads{state="timed-waiting",} 3.0
jvm_threads_states_threads{state="new",} 0.0
jvm_threads_states_threads{state="terminated",} 0.0
# HELP jvm_buffer_memory_used_bytes An estimate of the
```