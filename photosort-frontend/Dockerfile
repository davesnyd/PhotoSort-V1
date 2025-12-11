# PhotoSort Frontend Dockerfile
# Copyright 2025, David Snyderman
#
# Multi-stage build for React application
# Stage 1: Build the React app
# Stage 2: Serve with nginx

# Stage 1: Build
FROM node:18-alpine AS builder

WORKDIR /build

# Copy package files and install dependencies (cached layer)
COPY package*.json ./
RUN npm install

# Copy source code
COPY public ./public
COPY src ./src

# Build argument for API URL (can be overridden)
ARG REACT_APP_API_BASE_URL=http://localhost:8080
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL

# Build the application
RUN npm run build

# Stage 2: Production server with nginx
FROM nginx:1.25-alpine

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built React app from builder stage
COPY --from=builder /build/build /usr/share/nginx/html

# Create non-root user for nginx
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

# Switch to non-root user
USER nginx

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:3000 || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
