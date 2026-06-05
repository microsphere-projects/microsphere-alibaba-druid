# Release Notes

## v0.2.5

# Release Notes - Version 0.2.5

## New Features
- Simplified GitHub Actions merge workflow using branch matrix. [1092324](https://github.com/your-repo/commit/1092324)
- Automated release notes generation incorporated into workflow. [7028ee0](https://github.com/your-repo/commit/7028ee0)

## Other Changes
- Updated `microsphere-spring-cloud` dependency to version 0.2.10. [4742608](https://github.com/your-repo/commit/4742608)
- Updated Maven wrapper to version 3.9.15. [0a2473a](https://github.com/your-repo/commit/0a2473a)
- Added read permissions to Maven workflow. [74bcedf](https://github.com/your-repo/commit/74bcedf)
- Updated README with latest branch versions. [20660ce](https://github.com/your-repo/commit/20660ce)

---

For a full list of changes, please check the [commit history](https://github.com/your-repo/compare/v0.2.4...v0.2.5).

## v0.2.6

# Release Notes - Version 0.2.6

## Dependency Updates
- **Bumped `microsphere-spring-cloud` to `0.2.11`.** ([df4433b](df4433b))

## Documentation
- Added a **full changelog**, improved release notes, and dropped the 'v' prefix from version tags. ([9c147b8](9c147b8))
- **Updated branch latest versions** in the README file. ([6bf20af](6bf20af))

## Bug Fixes
- Fixed **formatting issues** in `dependabot.yml` updates list. ([9f719ca](9f719ca))

## Build and Workflow Enhancements
- Simplified **header wording** in `generate-wiki-docs.py` for easier maintenance. ([5713739](5713739))

## Other Changes
- Routine version bump to prepare for the next patch. ([e5b5939](e5b5939))  
- Multiple merges between `main` and `release` branches. *(Skipped CI)*

--- 
For a comprehensive changelog, refer to the repository.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.5...0.2.6## v0.2.7

# Release Notes - Version 0.2.7

## New Features
- **Alibaba Druid Support**: Added a JUnit 5 test extension and utility functions for Alibaba Druid. (#5bc0a4f)

## Bug Fixes
- Properly close `druidDataSource` in tests to prevent resource leaks. (#50f12c1)

## Documentation
- Updated README:
  - Added a link to Alibaba Druid.
  - Reformatted the introduction for improved clarity. (#ef3d2ab, #ee31178)

## Dependency Updates
- Switched to the `microsphere-java-test` dependency. (#3eaf4b3)

## Test Improvements
- Introduced separate class and method stores in the test extension for better organization. (#a8eb118)

## Build and Workflow Enhancements
- Added OSSRH credentials for CI publishing. (#91ed6fe)
- Updated Maven workflows in GitHub Actions to improve CI/CD processes. (#25e457f)

---

*Full Changelog*: [v0.2.6...v0.2.7](https://github.com/your-repo/compare/v0.2.6...v0.2.7)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.6...0.2.7## v0.2.9

# Release Notes: Version 0.2.9

## New Features
- Add `DruidDataSourcePoolMetadata` and its provider. (#fd688cb)

## Test Improvements
- Add unit tests for `DruidDataSourcePoolMetadata`. (#9262384)

## Other Changes
- Internal version bump post-0.2.7 release. (#23b1d5c)

---

**Note**: Changes labeled with `[skip ci]` were related to branch merges and do not introduce functional updates.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.7...0.2.9## v0.2.10

# Release Notes for Version 0.2.10

### Documentation
- Added comprehensive JavaDoc with examples to all main source files. [#86](https://github.com/microsphere-projects/microsphere-alibaba-druid)  
- Fixed typo in `AlibabaDruidTestUtils` JavaDoc (`defaultt` → `default`).  

### Dependency Updates
- Bumped `microsphere-spring-cloud` to version 0.2.12.  

### Test Improvements
- Removed `logback.xml` from test resources to streamline test configurations.  

### Other Changes
- Updated branches and version information in `README`.  
- Routine merges between `main` and `release` branches to ensure consistency.  

**Note:** This release is primarily focused on documentation improvements and minor configurations.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.9...0.2.10## v0.2.11

# Release Notes - Version 0.2.11

## New Features
- **Druid Integration**: Automatically register Druid Filters using `spring.factories`. (#79ad584)

## Build and Workflow Enhancements
- Merged `main` into `release` and vice versa to maintain branch consistency. (#87770cf, #0844fae)
- Bumped version post-release to prepare for the next development cycle. (#219d481)

---

**Note**: Full changelog available in the repository.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.10...0.2.11## v0.2.12

# Release Notes - Version 0.2.12

## New Features
- Add `.github` prompt templates for common development tasks. ([5561bba](#))

## Bug Fixes
- Fix badge links and formatting in `README.md`. ([5fb714d](#))

## Documentation
- Update `README.md` branch versions to 0.2.12 and 0.1.12. ([5c480bd](#))

## Dependency Updates
- Bump `microsphere spring-cloud` to version 0.2.13. ([4196424](#))

## Build and Workflow Enhancements
- Various merges between `main` and `release` branches to keep branches in sync. ([211177f](#), [84ccb93](#), [8916ebd](#))

## Other Changes
- Chore: Version bump to next patch after publishing 0.2.11. ([2db277a](#))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.11...0.2.12## v0.2.13

# Release Notes - Version 0.2.13

## New Features
- **BeanSource Support**: Added support for `BeanSource` and renamed `filterClasses` for improved functionality. ([81b8043](https://github.com/microsphere-projects/microsphere/commit/81b8043))

## Bug Fixes
- Removed duplicated line separators and resolved trailing whitespace issues across the Java source codebase. ([1657100](https://github.com/microsphere-projects/microsphere/commit/1657100))

## Documentation
- Updated README to reflect the latest branch versions. ([7ccbb75](https://github.com/microsphere-projects/microsphere/commit/7ccbb75))

## Dependency Updates
- Upgraded `microsphere-spring-cloud` to version `0.2.14`. ([bfa0583](https://github.com/microsphere-projects/microsphere/commit/bfa0583))

## Build and Workflow Enhancements
- Multiple merges and version bumps to synchronize branches and prepare for the next release cycle. ([67b650d](https://github.com/microsphere-projects/microsphere/commit/67b650d), [6a8716c](https://github.com/microsphere-projects/microsphere/commit/6a8716c), [f47e1e5](https://github.com/microsphere-projects/microsphere/commit/f47e1e5), [90b53cf](https://github.com/microsphere-projects/microsphere/commit/90b53cf), [836c067](https://github.com/microsphere-projects/microsphere/commit/836c067))
- Incremented project version to `0.2.14` post-release. ([f35e50b](https://github.com/microsphere-projects/microsphere/commit/f35e50b), [388c780](https://github.com/microsphere-projects/microsphere/commit/388c780)) 

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.12...0.2.13## v0.2.24

# Release Notes - Version 0.2.24

## New Features
- Added `DruidDataSource` for enhanced data source capabilities. ([98c4a15](#))

## Dependency Updates
- Bumped `microsphere-spring-cloud` to version `0.2.15`. ([549fde2](#))

## Documentation
- Updated README with branch version number updates. ([7e6782a](#))

## Build and Workflow Enhancements
- Merged `main` into `release`. ([5eb471a](#), [d2ebef0](#), [f25c410](#))
- Bumped to the next patch version post-publishing `0.2.13`. ([35a5116](#))
- Merged `release` into `main`. ([dcfdbbd](#))

## Other Changes
- Removed unused imports. ([98c4a15](#))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.13...0.2.24## v0.2.15

```markdown
# Release Notes - Version 0.2.15

## Changelog

### Other Changes
- chore: bump version to next patch after publishing 0.2.24 (ee3338d)
```

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.24...0.2.15## v0.2.16

# Release Notes - Version 0.2.16

### Build and Workflow Enhancements
- Merged main into release and release into main. [skip ci]
- Bumped version to next patch after publishing 0.2.15.

### Dependency Updates
- Updated Microsphere versions to `0.2.16` / `0.1.16`. 

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.15...0.2.16