# Release Notes

## v0.1.5

# Release Notes - Version 0.1.5

## New Features
- Added automated release notes generation in the publishing workflow. ([a468466](https://github.com/microsphere-projects/commit/a468466))

## Other Changes
- Updated Maven wrapper to version 3.9.15. ([d41a616](https://github.com/microsphere-projects/commit/d41a616))
- Bumped Microsphere BOM and Druid dependency versions. ([ced4783](https://github.com/microsphere-projects/commit/ced4783))
- Updated parent project version to 0.1.10. ([8e255b0](https://github.com/microsphere-projects/commit/8e255b0))
- Limited workflow permissions to `contents: read`. ([78c4bdb](https://github.com/microsphere-projects/commit/78c4bdb))
- Refreshed branch version details in README. ([24fb151](https://github.com/microsphere-projects/commit/24fb151)) 

---

*This release focuses on dependency updates, CI/CD improvements, and maintenance-related updates.*

## v0.1.6

# Release Notes - Version 0.1.6

## Dependency Updates
- Bumped `microsphere-spring-cloud` version to `0.1.11`. (#daed34a)
- Updated parent POM version to `0.1.11`. (#e13d7a1)

## Bug Fixes
- Fixed YAML indentation in Dependabot configuration. (#2a242a7)

## Build and Workflow Enhancements
- Improved release notes and release creation process. (#3d8029c)
- Merged `release-1.x` into `dev-1.x` for better branch alignment. (#2707f87)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.5...0.1.6## v0.1.7

# Release Notes - Version 0.1.7

## New Features
- Added Alibaba Druid JUnit test utilities for enhanced database testing. ([f81c49c](https://github.com/mercyblitz/project/commit/f81c49c))

## Documentation
- Updated README.md with the latest versions and setup details. ([03f1b7a](https://github.com/mercyblitz/project/commit/03f1b7a))

## Test Improvements
- Enhanced CI by fixing Java setup and Maven wrapper issues. ([8df92b5](https://github.com/mercyblitz/project/commit/8df92b5))

## Build and Workflow Enhancements
- Configured Maven publishing with server credentials for streamlined deployment. ([95437ac](https://github.com/mercyblitz/project/commit/95437ac))

## Other Changes
- Merged `release-1.x` into `dev-1.x` for branch consolidation. ([3cd44ab](https://github.com/mercyblitz/project/commit/3cd44ab))
- Set up post-release version bump after publishing 0.1.6. ([85976a7](https://github.com/mercyblitz/project/commit/85976a7))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.6...0.1.7## v0.2.8

# Release Notes: Version 0.2.8

## New Features
- **Druid Database Integration**
  - Added `DruidDataSourcePoolMetadata` provider for enhanced support.

## Test Improvements
- Added unit tests for `DruidDataSourcePoolMetadata`.

## Build and Workflow Enhancements
- Merged updates from `release-1.x` to `dev-1.x` for improved development workflow.
- Version bumped to `0.2.8` after publishing `0.1.7`.

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.7...0.2.8## v0.1.9

```markdown
# Release Notes - Version 0.1.9

## Other Changes
- **chore**: Bumped version to next patch after publishing 0.2.8.
```

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.8...0.1.9## v0.1.10

# Release Notes - v0.1.10

## Documentations
- Updated README branch version numbers and names in the table for improved clarity. ([855da9a](...), [ad32e94](...))

## Dependency Updates
- Bumped `microsphere-spring-cloud` dependency to version 0.1.12. ([32bbd16](...))

## Test Improvements
- Removed `logback.xml` from test resources to streamline configurations. ([1372c3d](...))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` for workflow consistency. ([712e02e](...), [af3b9ec](...))
- Bumped version to next patch after publishing v0.1.9. ([703cb71](...))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.9...0.1.10## v0.1.11

# Release Notes - Version 0.1.11

## 🚀 New Features
- Register filters from `spring.factories`. ([#88](https://github.com/mercyblitz/dev-1.x))

## 📝 Documentation
- Added documentation for filter registration.

## 🔧 Other Changes
- Merged `release-1.x` into `dev-1.x` for branch maintenance.  
- Bumped version to prepare for the next patch release.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.10...0.1.11## v0.1.12

# Release Notes: Version 0.1.12

## Build and Workflow Enhancements
- Added `.github` AI prompt templates for improved contribution guidance. (#54d7212)  

## Documentation
- Updated `README.md` to reflect the latest version numbers and branch updates. (#e8d8af4, #ae92dcd)  

## Other Changes
- Merged `release-1.x` and `dev-1.x` branches for consistency and integration. (#6d74801, #8e74d1a)  
- Updated project version to 0.1.13 post-release of 0.1.11. (#4608310, #6acedcd)  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.11...0.1.12## v0.1.13

# Release Notes for Version 0.1.13

## New Features
- **Support multiple filter sources**: Added functionality to support multiple filter sources for enhanced flexibility. ([fbd74cd](https://example.com/commit/fbd74cd))

## Bug Fixes
- **Code cleanup**: Removed duplicated line separators and trailing whitespace. ([bcf7b93](https://example.com/commit/bcf7b93))

## Test Improvements
- Added comprehensive tests for multiple filter sources feature. ([fbd74cd](https://example.com/commit/fbd74cd))

## Build and Workflow Enhancements
- **Version management**: Bumped parent version to `0.1.14` for future development. ([1cb6723](https://example.com/commit/1cb6723))
- **Branch management**: Merged `release-1.x` into `dev-1.x` to ensure branch consistency. ([a9e27c0](https://example.com/commit/a9e27c0))
- Updated to next patch version `0.1.13` after publishing `0.1.12`. ([a734f2d](https://example.com/commit/a734f2d))

--- 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.12...0.1.13## v0.1.14

# Release Notes - Version 0.1.14  

## Dependency Updates  
- Upgraded `microsphere-spring-cloud` to version `0.1.15` (fddea29).  

## Code Maintenance  
- Bumped versions and tidied imports for improved project consistency (97e7d48).  
- Merged `release-1.x` into `dev-1.x` to synchronize branches (fa85b1b).  
- Updated project version to prepare for the next patch release (4f16f4b).  

## Other Changes  
- Integrated changes from `dev-1.x` branch into the project (97223de).  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.13...0.1.14## v0.1.15

# Release Notes - Version 0.1.15

## Other Changes
- **Chore**: Bumped version to next patch after publishing 0.1.14 (`eeadf70`)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.14...0.1.15## v0.1.16

# Release Notes - Version 0.1.16

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` to sync branches. [#94]  
- Bumped version to `0.1.16` and updated project parent POM.  

## Documentation
- Updated `README` to reflect changes in version `0.1.16`.  

---

Full Changelog: [v0.1.15...v0.1.16](https://github.com/mercyblitz/your-repo/compare/v0.1.15...v0.1.16)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.15...0.1.16## v0.1.17

# Release Notes for Version 0.1.17

## 📦 New Features
- **AlibabaDruidRegistrar**: Refactored base class for improved architecture. (#670e9ae)
- **DruidDataSourceBeanPostProcessor**: Set bean name to enhance bean registration process. (#e0472f9)
- Introduced conditional configuration with `@ConditionalOnFeaturesAvailable`. (#846307d)

## 📚 Documentation
- Updated README to reflect latest branch versions. (#efedb95)
- Cleaned up redundant blank lines in README. (#2ec99bf)

## 🔗 Dependency Updates
- Upgraded `microsphere-spring-cloud` to version 0.1.21. (#06e86c8)
- Previous incremental updates of `microsphere-spring-cloud` to 0.1.19 and 0.1.20. (#6811888, #67e0e72)

## 🛠️ Other Changes
- Removed stray backtick from `pom.xml`. (#304bcfb)
- Merged release branch updates into development branch. (#169c8d1)

---

**Note**: This version includes both functional enhancements and cleanup efforts to improve project maintainability.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.1.16...0.1.17