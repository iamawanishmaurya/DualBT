# Route Plan Test Expected Unencoded Name Solution

- Problem: [2026-05-24-route-plan-test-expected-unencoded-name.md](../problems/2026-05-24-route-plan-test-expected-unencoded-name.md)
- What failed: `StreamRoutePlanTest` asserted that the serialized payload contained the literal display name `Kitchen Speaker`.
- What worked: Update the payload assertion to expect `Kitchen+Speaker`, then rely on the existing round-trip parse assertions to verify decoded display names and addresses.
- Why it worked: `StreamRoutePlan` intentionally URL-encodes fields so route payload delimiters and punctuation remain unambiguous when passed through service extras.
- Commands run:

```bash
mkdir -p /tmp/dualbt-route-plan-test/classes-green-1350
javac -cp app/src/main/java -d /tmp/dualbt-route-plan-test/classes-green-1350 app/src/test/java/com/xpwnit/dualbt/state/StreamRoutePlanTest.java app/src/main/java/com/xpwnit/dualbt/state/StreamDevice.java app/src/main/java/com/xpwnit/dualbt/state/StreamRoutePlan.java
java -cp /tmp/dualbt-route-plan-test/classes-green-1350 com.xpwnit.dualbt.state.StreamRoutePlanTest
```
