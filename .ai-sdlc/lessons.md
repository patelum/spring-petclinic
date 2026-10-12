# Lessons for this repository

<!-- Repo-specific facts and corrections learned while delivering tickets. Every agent reads this file.
     Upgrades never overwrite it. Entries are added by "Improve the process" (after your y) or by hand.
     Keep each entry one or two lines, with its source. Remove entries that stop being true. -->

<!-- Example:
- `messages_en.properties` is intentionally empty; English text lives in `messages.properties`. (PET-12 retro)
-->

- I18nPropertiesSyncTest rejects hardcoded text in templates, including `&nbsp;` in an empty select option. Put the text in the messages files. The test is in the system package (src/test/java/org/springframework/samples/petclinic/system/I18nPropertiesSyncTest.java), so the scoped `verify_module` run for owner/vet does not run it. After any template change, run it with `test_one` (`-Dtest=org.springframework.samples.petclinic.system.I18nPropertiesSyncTest`). (source: PET-12)
