# Contributing to Collaborative Project Management System

## How to Contribute

### 1. Code Style

- Follow Java conventions (camelCase for variables, PascalCase for classes)
- **IMPORTANT**: JSON responses must use camelCase field names
- Use meaningful variable names
- Add comments for complex logic

### 2. JSON Field Names (CRITICAL)

Always use camelCase in JSON responses:
```javascript
✅ CORRECT:
{ "firstName": "Alice", "currentLoad": 45.5, "teamId": 1 }

❌ WRONG:
{ "first_name": "Alice", "current_load": 45.5, "team_id": 1 }
```

### 3. Database Changes

- Add migration scripts to `sql/` folder
- Always update the `sql/database_complete.sql` file
- Name scripts clearly: `add_[feature_name].sql`
- Test migrations thoroughly before committing

### 4. API Changes

- Update REST endpoints in servlet files (`src/servlet/`)
- Return proper HTTP status codes (200, 201, 400, 404, 500)
- Always return JSON with proper `Content-Type: application/json`
- Document changes in `docs/API_DOCUMENTATION.md`

### 5. Frontend Changes

- Update `frontend/js/api-client.js` for new API calls
- Add corresponding functions in `frontend/js/app.js`
- Update HTML pages in `frontend/html/`
- Test in multiple browsers
- Use cache-busting timestamps for API calls

### 6. Testing

Before pushing:
1. Test your changes locally with `mvn jetty:run`
2. Verify database changes work
3. Check browser console for JavaScript errors
4. Test with fresh browser cache (Ctrl+Shift+R)
5. Follow scenarios in `docs/TESTING_GUIDE.md`

### 7. Commit Messages

Use clear, descriptive commit messages:
```
feat: Add skill-based task auto-assignment
fix: Resolve logout session invalidation issue
docs: Update API documentation
chore: Upgrade Maven dependencies
```

### 8. Pull Requests

- Create a feature branch: `git checkout -b feature/your-feature`
- Make focused changes (one feature per PR)
- Update relevant documentation
- Include testing steps in PR description
- Request review before merging

## Project Structure Guidelines

### Adding a New Entity

1. Create class in `src/classes/EntityName.java`
2. Create DAO in `src/dao/EntityNameDAO.java`
3. Create servlet in `src/servlet/EntityNameServlet.java`
4. Add API wrapper in `frontend/js/api-client.js`
5. Update `frontend/js/app.js` with load/display functions
6. Create database table in `sql/`
7. Document in `docs/API_DOCUMENTATION.md`

### Modifying Task Assignment Algorithm

Location: `src/service/TaskAssignmentService.java`

Key methods:
- `findBestMemberForTask()` - Scoring algorithm
- `assignTasksAutomatically()` - Main workflow
- `calculateSkillMatch()` - Skill matching score

Update documentation when changing algorithm logic.

## Common Issues

### "JSON field not found" errors
Check that camelCase is used in servlet responses and JavaScript expects camelCase.

### Database connection fails
Verify `src/classes/Connect.java` has correct credentials and MySQL is running.

### Frontend not updating
Ensure cache-busting timestamp `?_t=${timestamp}` is used in fetch calls.

## Documentation Standards

- Update relevant `.md` files when making changes
- Keep API documentation in sync with code
- Document complex algorithms with comments
- Include examples for new features

## Questions?

Open an issue or contact the development team for clarification.

---

Thank you for contributing! 🎉
