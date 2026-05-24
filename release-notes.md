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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.9...0.2.10