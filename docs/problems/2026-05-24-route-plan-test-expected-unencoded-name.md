# Route Plan Test Expected Unencoded Name

- Timestamp: 2026-05-24 13:51:14 IST
- Exact error:

```text
Exception in thread "main" java.lang.AssertionError: payload contains first name: expected Kitchen+Speaker|Bonded+Bluetooth+loudspeaker|AA%3ABB%3ACC%3A00%3A01
Desk+Speaker|Bonded+Bluetooth+hi-fi+audio|AA%3ABB%3ACC%3A00%3A02 to contain Kitchen Speaker
	at com.xpwnit.dualbt.state.StreamRoutePlanTest.assertContains(StreamRoutePlanTest.java:35)
	at com.xpwnit.dualbt.state.StreamRoutePlanTest.main(StreamRoutePlanTest.java:15)
```

- Reproduction steps:
  1. Compile `StreamRoutePlanTest`, `StreamDevice`, and `StreamRoutePlan`.
  2. Run `java -cp /tmp/dualbt-route-plan-test/classes-green-1350 com.xpwnit.dualbt.state.StreamRoutePlanTest`.
  3. Observe that the payload assertion expects an unencoded display name.
- Environment:
  - Workspace: `/home/astra/codex/DualBT`
  - JDK: OpenJDK 21
  - Test output directory: `/tmp/dualbt-route-plan-test/classes-green-1350`
- First hypothesis: The test expectation is wrong because route payload fields are intentionally URL-encoded; the assertion should check the encoded name and round-trip parsing should verify the decoded display name.
