# Frontend Automated Testing Implementation Plan
**Created**: 2025-12-02
**Status**: Ready to Implement
**Scope**: Add Jest + React Testing Library tests for all frontend components (Steps 5-7)

## Overview
Implement comprehensive automated frontend testing to match the backend testing rigor. This will provide confidence that UI components work correctly and catch regressions early.

---

## Phase 1: Setup & Configuration (30 minutes)

### 1.1 Install Dependencies
```bash
cd photosort-frontend
npm install --save-dev @testing-library/react @testing-library/jest-dom @testing-library/user-event @testing-library/hooks
```

**Note**: Create React App already includes Jest, so no additional Jest installation needed.

### 1.2 Create Test Utilities
**File**: `photosort-frontend/src/setupTests.js`
```javascript
// Already exists in CRA - verify it imports @testing-library/jest-dom
import '@testing-library/jest-dom';
```

**File**: `photosort-frontend/src/test-utils/testUtils.js`
```javascript
import React from 'react';
import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';

// Wrapper for components that need routing and auth context
export function renderWithProviders(ui, options = {}) {
  const Wrapper = ({ children }) => (
    <BrowserRouter>
      <AuthProvider>
        {children}
      </AuthProvider>
    </BrowserRouter>
  );

  return render(ui, { wrapper: Wrapper, ...options });
}

// Mock fetch for API calls
export const mockFetch = (data) => {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve(data),
    })
  );
};

// Mock axios for API service
export const mockAxios = (axiosInstance) => {
  axiosInstance.get = jest.fn();
  axiosInstance.post = jest.fn();
  axiosInstance.put = jest.fn();
  axiosInstance.delete = jest.fn();
  return axiosInstance;
};
```

### 1.3 Configure Jest (if needed)
**File**: `photosort-frontend/package.json`
```json
{
  "jest": {
    "collectCoverageFrom": [
      "src/**/*.{js,jsx}",
      "!src/index.js",
      "!src/reportWebVitals.js"
    ],
    "coverageThreshold": {
      "global": {
        "branches": 70,
        "functions": 70,
        "lines": 70,
        "statements": 70
      }
    }
  }
}
```

---

## Phase 2: Test Implementation - Priority Order

### 2.1 Custom Hooks (HIGHEST PRIORITY)
These are the foundation - test first!

#### **useTableData.test.js**
**Location**: `photosort-frontend/src/hooks/useTableData.test.js`

**Test Cases**:
1. ✅ Fetches data on mount
2. ✅ Shows loading state during fetch
3. ✅ Updates data when fetch succeeds
4. ✅ Shows error when fetch fails
5. ✅ Handles page change
6. ✅ Handles sort change
7. ✅ Handles quick search
8. ✅ Handles advanced search
9. ✅ Refresh function re-fetches data
10. ✅ Resets to page 0 when search changes

**Code Structure**:
```javascript
import { renderHook, act, waitFor } from '@testing-library/react';
import useTableData from './useTableData';

describe('useTableData Hook', () => {
  const mockFetchFunction = jest.fn();

  beforeEach(() => {
    mockFetchFunction.mockClear();
  });

  it('fetches data on mount', async () => {
    mockFetchFunction.mockResolvedValue({
      data: { content: [], totalPages: 0, totalElements: 0 }
    });

    const { result } = renderHook(() =>
      useTableData(mockFetchFunction, { field: 'id', direction: 'asc' }, 10)
    );

    expect(result.current.loading).toBe(true);

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(mockFetchFunction).toHaveBeenCalledTimes(1);
  });

  // ... 9 more test cases
});
```

**Estimated Lines**: ~250

---

### 2.2 Generic Components (HIGH PRIORITY)

#### **DataTable.test.js**
**Location**: `photosort-frontend/src/components/DataTable.test.js`

**Test Cases**:
1. ✅ Renders with empty data
2. ✅ Renders rows with provided data
3. ✅ Renders custom column renderers
4. ✅ Calls onSort when sortable column clicked
5. ✅ Shows sort indicator on current sort column
6. ✅ Renders action buttons via renderActions prop
7. ✅ Shows "no data" message when empty
8. ✅ Uses correct keyField for row keys

**Mock Data**:
```javascript
const mockColumns = [
  { field: 'id', header: 'ID', sortable: true },
  { field: 'name', header: 'Name', sortable: true },
  { field: 'status', header: 'Status', sortable: false, render: (row) => <span>{row.status}</span> }
];

const mockData = [
  { id: 1, name: 'Test 1', status: 'active' },
  { id: 2, name: 'Test 2', status: 'inactive' }
];
```

**Estimated Lines**: ~200

#### **TablePage.test.js**
**Location**: `photosort-frontend/src/components/TablePage.test.js`

**Test Cases**:
1. ✅ Renders title and subtitle
2. ✅ Renders children content
3. ✅ Applies correct CSS classes

**Estimated Lines**: ~50

#### **SearchControls.test.js**
**Location**: `photosort-frontend/src/components/SearchControls.test.js`

**Test Cases**:
1. ✅ Renders quick search input and button
2. ✅ Calls onQuickSearch when search button clicked
3. ✅ Calls onQuickSearch when Enter key pressed
4. ✅ Shows/hides advanced search panel
5. ✅ Calls onAdvancedSearch with correct filter data
6. ✅ Clears search inputs when clear button clicked

**Estimated Lines**: ~150

#### **PaginationControls.test.js**
**Location**: `photosort-frontend/src/components/PaginationControls.test.js`

**Test Cases**:
1. ✅ Renders correct page numbers
2. ✅ Calls onPageChange with correct page
3. ✅ Disables Previous on first page
4. ✅ Disables Next on last page
5. ✅ Shows current page highlighted

**Estimated Lines**: ~100

---

### 2.3 Feature-Specific Components (MEDIUM PRIORITY)

#### **UserTable.test.js**
**Location**: `photosort-frontend/src/components/UserTable.test.js`

**Test Cases**:
1. ✅ Renders user data in table
2. ✅ Shows user type badge with correct styling
3. ✅ Shows Edit button in view mode
4. ✅ Shows dropdown when Edit clicked
5. ✅ Shows Save/Cancel buttons in edit mode
6. ✅ Calls onUserTypeChange with correct data
7. ✅ Cancels edit mode without changes
8. ✅ Formats dates correctly
9. ✅ Navigates to photos page when View Images clicked

**Estimated Lines**: ~250

#### **PhotoTable.test.js**
**Location**: `photosort-frontend/src/components/PhotoTable.test.js`

**Test Cases**:
1. ✅ Renders photo data in table
2. ✅ Displays thumbnails correctly
3. ✅ Shows "No Image" placeholder for missing thumbnails
4. ✅ Formats file sizes correctly (KB/MB/GB)
5. ✅ Formats dimensions correctly
6. ✅ Shows public/private badge with correct styling
7. ✅ Opens full image in new tab when View clicked
8. ✅ Formats dates correctly

**Estimated Lines**: ~200

---

### 2.4 Page Components (MEDIUM PRIORITY)

#### **Users.test.js**
**Location**: `photosort-frontend/src/pages/Users.test.js`

**Test Cases**:
1. ✅ Renders loading state
2. ✅ Renders error state
3. ✅ Renders user table when data loaded
4. ✅ Shows results summary
5. ✅ Handles user type change
6. ✅ Refreshes data after update
7. ✅ Integration: search updates table

**Mock Setup**:
```javascript
jest.mock('../services/userService');
jest.mock('../hooks/useTableData');
```

**Estimated Lines**: ~200

#### **Photos.test.js**
**Location**: `photosort-frontend/src/pages/Photos.test.js`

**Test Cases**:
1. ✅ Renders loading state
2. ✅ Renders error state
3. ✅ Renders photo table when data loaded
4. ✅ Shows results summary
5. ✅ Shows correct title for user-filtered view
6. ✅ Uses userId parameter when provided
7. ✅ Integration: search updates table

**Estimated Lines**: ~200

#### **Home.test.js**
**Location**: `photosort-frontend/src/pages/Home.test.js`

**Test Cases**:
1. ✅ Renders welcome message
2. ✅ Displays user name from auth context
3. ✅ Shows feature cards
4. ✅ Renders all feature descriptions

**Estimated Lines**: ~80

#### **Login.test.js**
**Location**: `photosort-frontend/src/pages/Login.test.js`

**Test Cases**:
1. ✅ Renders login form
2. ✅ Shows Google sign-in button
3. ✅ Navigates to OAuth URL when button clicked

**Estimated Lines**: ~60

---

### 2.5 Service/API Layer (LOW PRIORITY - Can Mock)

#### **userService.test.js**
**Location**: `photosort-frontend/src/services/userService.test.js`

**Test Cases**:
1. ✅ getUsers calls API with correct params
2. ✅ updateUserType calls PUT with correct data
3. ✅ Handles API errors correctly

**Estimated Lines**: ~100

#### **photoService.test.js**
**Location**: `photosort-frontend/src/services/photoService.test.js`

**Test Cases**:
1. ✅ getPhotos calls API with correct params
2. ✅ getUserColumns calls API with userId
3. ✅ updateUserColumns calls PUT with correct data
4. ✅ Handles API errors correctly

**Estimated Lines**: ~120

---

## Phase 3: Test Execution & Coverage

### 3.1 Run Tests
```bash
# Run all tests
npm test

# Run with coverage
npm test -- --coverage

# Run specific test file
npm test UserTable.test.js

# Run in watch mode (for development)
npm test -- --watch
```

### 3.2 Coverage Goals
- **Hooks**: 90%+ coverage (critical business logic)
- **Generic Components**: 85%+ coverage (reused everywhere)
- **Feature Components**: 80%+ coverage
- **Pages**: 70%+ coverage (more integration-focused)
- **Services**: 75%+ coverage

### 3.3 Update package.json Scripts
```json
{
  "scripts": {
    "test": "react-scripts test",
    "test:coverage": "react-scripts test --coverage --watchAll=false",
    "test:ci": "CI=true react-scripts test --coverage"
  }
}
```

---

## Phase 4: Integration with Development Workflow

### 4.1 Update CLAUDE.md

Add to **New Functionality Implementation Steps** after "Initial Testing":

```markdown
## 2. Initial Testing

### Backend Testing
Review the initial test cases listed in the current step of "PhotoSpecification.md".
Implement them using JUnit tests with Spring Boot Testing Utilities.

### Frontend Testing
For any new React components or hooks:
1. Create corresponding `.test.js` file
2. Implement unit tests covering:
   - Component renders correctly
   - User interactions work (clicks, typing, etc.)
   - Props are handled correctly
   - Error states are handled
3. Aim for 80%+ code coverage for new code
4. Run tests: `npm test -- --coverage`

Confirm that tests fail (red phase of TDD) before implementing functionality.
```

### 4.2 Add to Test Case Augmentation (Step 5)

```markdown
## 5. Test Case Augmentation

### Backend
Review the functionality created and determine additional JUnit test cases...

### Frontend
Review React components and add tests for:
- Edge cases (empty data, null values, undefined props)
- Error conditions (API failures, validation errors)
- User interactions (button clicks, form submissions)
- Accessibility (ARIA labels, keyboard navigation)

Run: `npm test -- --coverage` to verify coverage meets goals.
```

### 4.3 Update .gitignore

Ensure these are in `.gitignore`:
```
# Testing
coverage/
.nyc_output/
```

---

## Phase 5: Test Data & Mocks

### 5.1 Create Mock Data File
**Location**: `photosort-frontend/src/test-utils/mockData.js`

```javascript
export const mockUsers = [
  {
    userId: 1,
    email: 'user1@example.com',
    displayName: 'User One',
    userType: 'USER',
    photoCount: 5,
    firstLoginDate: '2024-01-15T10:30:00',
    lastLoginDate: '2024-12-01T14:20:00'
  },
  {
    userId: 2,
    email: 'admin@example.com',
    displayName: 'Admin User',
    userType: 'ADMIN',
    photoCount: 10,
    firstLoginDate: '2024-01-10T09:00:00',
    lastLoginDate: '2024-12-02T08:15:00'
  }
];

export const mockPhotos = [
  {
    photoId: 1,
    fileName: 'vacation.jpg',
    filePath: '/photos/vacation.jpg',
    fileSize: 2048576,
    imageWidth: 1920,
    imageHeight: 1080,
    fileCreatedDate: '2024-06-15T12:00:00',
    ownerDisplayName: 'User One',
    ownerId: 1,
    isPublic: true,
    thumbnailPath: '/thumbnails/vacation.jpg'
  },
  {
    photoId: 2,
    fileName: 'private.jpg',
    filePath: '/photos/private.jpg',
    fileSize: 1048576,
    imageWidth: 1280,
    imageHeight: 720,
    fileCreatedDate: '2024-07-20T15:30:00',
    ownerDisplayName: 'Admin User',
    ownerId: 2,
    isPublic: false,
    thumbnailPath: '/thumbnails/private.jpg'
  }
];

export const mockPagedResponse = (data) => ({
  content: data,
  page: 0,
  pageSize: 10,
  totalPages: 1,
  totalElements: data.length
});

export const mockApiResponse = (data) => ({
  success: true,
  data: data,
  error: null
});

export const mockApiError = (message) => ({
  success: false,
  data: null,
  error: {
    code: 'ERROR',
    message: message
  }
});
```

---

## Implementation Order Summary

### Week 1: Foundation
1. **Day 1**: Setup & Configuration (Phase 1)
2. **Day 2**: useTableData.test.js (most critical)
3. **Day 3**: DataTable.test.js + TablePage.test.js
4. **Day 4**: SearchControls.test.js + PaginationControls.test.js

### Week 2: Feature Tests
5. **Day 5**: UserTable.test.js
6. **Day 6**: PhotoTable.test.js
7. **Day 7**: Users.test.js + Photos.test.js
8. **Day 8**: Home.test.js + Login.test.js + Services

### Week 3: Polish
9. **Day 9**: Coverage analysis, fix gaps
10. **Day 10**: Update documentation, CLAUDE.md integration

---

## Success Criteria

✅ All new tests pass
✅ Coverage >= 80% for hooks and generic components
✅ Coverage >= 70% overall
✅ `npm test` runs without errors
✅ `npm run build` still succeeds
✅ Documentation updated (CLAUDE.md, TestPlan.md)
✅ Git commit with message: "Add comprehensive frontend automated tests"

---

## Estimated Effort

- **Setup**: 1 hour
- **Test Implementation**: 16-20 hours (writing ~1900 lines of tests)
- **Coverage Tuning**: 2-3 hours
- **Documentation**: 1 hour
- **TOTAL**: 20-24 hours

---

## Notes for Continuation

When you resume:
1. Start with Phase 1 setup
2. Create test-utils directory and files
3. Begin with useTableData.test.js (highest ROI)
4. Run tests frequently: `npm test -- --watch`
5. Check coverage after each component: `npm test -- --coverage --watchAll=false`
6. Commit tests alongside features going forward

**Priority**: Focus on hooks and generic components first - they're used everywhere and have the highest impact.
