<!--
PR title: "<gitmoji> <imperative summary>" in English, e.g. "✨ Add weather provider registry".
The leading gitmoji sets the label and the version bump through release-drafter:
💥 breaking, ✨ feature, 🐛 fix, 📝 docs, ♻️ refactor, 🔧 chore, ...
See .github/release-drafter.yml for the full mapping.
Add the skip-changelog label to keep this PR out of the release notes.
日本語での記入も歓迎します。
-->

## Summary

<!-- What does this PR change, and why? -->

## Related issues

Closes #

## Breaking changes

<!-- Changes to the config/JSON format, API/SPI, commands and events all count as breaking. -->

- [ ] This PR contains breaking changes

<!-- If checked, describe what breaks and how users should update. -->

## How it was tested

<!-- Commands you ran, tests you added, or how you checked it on a server. -->

## Checklist

- [ ] Ran `task fmt`, and `task check` passes
- [ ] If `docs/` changed, `pnpm run check:ci`, `pnpm run typecheck` and `pnpm run build` pass in `docs/`
- [ ] Updated user-facing docs if the behavior changed
- [ ] Follows the architecture rules in [`.agent/rules/`](https://github.com/morinoparty/MoripaFishing/tree/main/.agent/rules) (the source of CLAUDE.md)
