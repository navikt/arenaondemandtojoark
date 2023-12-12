# Arenaondemandtojoark
Arenaondemandtojoark er ein éingongsjobb som skal migrere Arena-dokument frå onDemand til Joark.

Appen mottek journaldata på XML-format og hentar tilhøyrande dokument frå onDemand. Dette blir journalført og arkivert i Joark.
Basert på dette blir det laga ein journalpostrapport, og denne skal bli lese av Arena-batchen GR81.

Appen er av typen Naisjob, og den blir starta manuelt frå Github Actions ved å gi inn filnamn og prosesseringssteget
ein har lyst til å utføre. Det er tre steg ein kan utføre: innlesing, prosessering og rapportering. 

Det er tenkt at handsaminga av ei fil skal utførast ved å trigge dei tre stega i tur og orden. Stega kan bli trigga 
i [Github Actions-workflowen for dev](https://github.com/navikt/arenaondemandtojoark/actions/workflows/run-operation-dev.yml) eller
i [Github Actions-workflowen for prod](https://github.com/navikt/arenaondemandtojoark/actions/workflows/run-operation-prod.yml).

## Førespurnadar
Spørsmål om koda eller prosjektet kan stillast på [Slack-kanalen for \#Team  Dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ).