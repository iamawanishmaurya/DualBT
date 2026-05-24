# Network Interface Inspection Blocked

- Timestamp: 2026-05-24 08:13:28 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, sandbox `workspace-write`.

## Exact Error

```text
Cannot open netlink socket: Operation not permitted
```

## Reproduction Steps

1. Run `ip addr` from `/home/astra/codex/DualBT`.
2. Observe the command exits with code 1 and the error above.

## First Hypothesis

The sandbox does not permit netlink socket access. This may also explain why Gradle cannot inspect or bind network interfaces for daemon communication.

