# Plain Java Test Source Omission Solution

## Linked Problem

- `docs/problems/2026-05-24-plain-java-test-source-omission.md`

## What Failed

The local `javac` test command omitted `app/src/main/java/com/xpwnit/dualbt/ui/SystemBarAppearancePolicy.java`, so `SystemBarAppearancePolicyTest` could not compile.

## What Worked

Adding `SystemBarAppearancePolicy.java` to the plain Java source list and rerunning all test mains worked.

## Why It Worked

The UI policy test is a plain Java test, but it depends on its production policy class. Including that production class lets `javac` resolve the symbol and produce the test class.

## Commands Run

```bash
rm -rf /tmp/dualbt-test-classes && mkdir -p /tmp/dualbt-test-classes
javac -d /tmp/dualbt-test-classes ... app/src/main/java/com/xpwnit/dualbt/ui/SystemBarAppearancePolicy.java $(find app/src/test/java -name '*Test.java' | sort)
for test in $(find app/src/test/java -name '*Test.java' | sort | sed 's#app/src/test/java/##; s#/#.#g; s#.java$##'); do java -cp /tmp/dualbt-test-classes "$test"; done
```
