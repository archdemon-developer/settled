# 0011. Frontend component structure

**Status:** Accepted

## Context

The UI started as flat `components/` and `views/` folders. Pages such as the account list and account detail each fetched data, held loading and error state, and rendered markup directly, so there was no shared place for reusable UI. The Transactions feature adds modals, tables and forms, and without a rule they would be built inline and copied. We wanted a structure organised by how reusable a component is, with an import direction that can be checked.

## Decision

Components are organised in four levels. Each level is built from the levels below it.

```
src/
  components/
    base/        BaseButton, BaseInput, BaseBadge ...   no domain, no data
    composite/   ConfirmModal, Accordion, DataTable ... built from base, no domain
    sections/
      layout/    AppHeader, AppSidebar, AppFooter
      accounts/  AccountCreateForm, AccountList, AccountDetail
  views/         AccountListPage, AccountDetailPage, TransactionListPage
  router/  api/  stores/  types/  composables/  utils/
```

| Level     | May import                                                       | Owns                                                           | Must not                                       |
| --------- | ---------------------------------------------------------------- | -------------------------------------------------------------- | ---------------------------------------------- |
| base      | composables, utils                                               | All styling (Tailwind) and accessibility (labels, aria, focus) | Know any domain term, touch a store or the API |
| composite | base, composables, utils                                         | Interaction patterns (open/close, sort, validation display)    | Know the domain, touch a store or the API      |
| sections  | base, composite, sections of the same domain, stores, api, types | Data fetching, domain logic, form state                        | Import a view                                  |
| views     | sections, router                                                 | Route params passed down as props                              | Hold logic or fetch data                       |

Rules:

- Imports point down the table only. A lower level never imports a higher one.
- Sections own their data. A page passes route params to a section as props, and the section fetches.
- Sections and views use base components instead of raw `<button>`, `<input>` or `<select>`. They may add layout classes (spacing, grid, width) but not colours, borders or typography.
- Small domain-aware pieces such as a status badge live in `sections/<domain>/` as small sections. A section may import another section from its own domain, and from another domain only if that file is a leaf that imports only base and composite.
- Extract by the rule of two. Build inline first, and move something into base or composite when a second place needs it. Never extract speculatively.
- Reusable stateful logic goes in `composables/`, pure helpers in `utils/`. Composables do not import components.
- No barrel `index.ts` files. Import by path.
- Naming: `Base*` for base, descriptive and domain-free for composite, domain-prefixed for sections, `*Page` for views.
- Tests sit next to the file. Base: render, props, emits. Composite: interactions. Sections: mock the API module. Views: a smoke test that the right sections render.
- Folders are created when they get their first file. `base/` and `composite/` do not exist yet.

The test for composite versus section is whether the component knows anything about the ledger. If it does, or if it is unclear, it is a section.

## Consequences

- Reusable pieces cannot quietly depend on ledger logic, and pages stay small.
- Loading and error state sit next to the code that loads, in the section.
- More folders and a rule to remember. The composite and sections boundary is a judgement call and will occasionally be argued.
- The import rules are enforced by ESLint `no-restricted-imports` overrides per folder in `eslint.config.mjs`, and cross-folder imports use the `@/` alias (parent-relative paths are banned). Raw `<button>` and `<input>` in sections and views are not machine-checked and stay a review convention.
- Moving a component between levels later means changing its imports, so the rule of two matters.
