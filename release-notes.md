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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-druid/compare/0.2.8...0.1.9