/**
 * Main App Component
 * Copyright 2025, David Snyderman
 *
 * Root component with routing and authentication context
 */

import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import Navigation from './components/Navigation';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import OAuthCallback from './pages/OAuthCallback';
import Home from './pages/Home';
import Users from './pages/Users';
import Photos from './pages/Photos';
import ImageDisplay from './pages/ImageDisplay';
import Scripts from './pages/Scripts';
import Configuration from './pages/Configuration';
import './App.css';

// Create a client for React Query
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <AuthProvider>
          <div className="App">
            <Navigation />
            <Routes>
              {/* Public routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/auth/callback" element={<OAuthCallback />} />

              {/* Protected routes */}
              <Route
                path="/"
                element={
                  <ProtectedRoute>
                    <Home />
                  </ProtectedRoute>
                }
              />

              {/* Placeholder routes for future features */}
              <Route
                path="/my-photos"
                element={
                  <ProtectedRoute>
                    <div style={{ padding: '40px', backgroundColor: '#FFFDD0', minHeight: '100vh' }}>
                      <h2 style={{ color: '#800020' }}>My Photos</h2>
                      <p style={{ color: '#000080' }}>This feature will be implemented in a future step.</p>
                    </div>
                  </ProtectedRoute>
                }
              />

              <Route
                path="/users"
                element={
                  <ProtectedRoute adminOnly={true}>
                    <Users />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/photos/:userId"
                element={
                  <ProtectedRoute adminOnly={true}>
                    <Photos />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/photo/:photoId"
                element={
                  <ProtectedRoute>
                    <ImageDisplay />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/photos"
                element={
                  <ProtectedRoute adminOnly={true}>
                    <div style={{ padding: '40px', backgroundColor: '#FFFDD0', minHeight: '100vh' }}>
                      <h2 style={{ color: '#800020' }}>Photo Management</h2>
                      <p style={{ color: '#000080' }}>This feature will be implemented in Step 7.</p>
                    </div>
                  </ProtectedRoute>
                }
              />

              <Route
                path="/scripts"
                element={
                  <ProtectedRoute adminOnly={true}>
                    <Scripts />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/configuration"
                element={
                  <ProtectedRoute adminOnly={true}>
                    <Configuration />
                  </ProtectedRoute>
                }
              />
            </Routes>
          </div>
        </AuthProvider>
      </Router>
    </QueryClientProvider>
  );
}

export default App;
