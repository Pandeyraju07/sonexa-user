FROM node:20-alpine

# Set working directory
WORKDIR /app

# Copy package definitions
COPY package*.json ./

# Install production dependencies
RUN npm install --omit=dev

# Copy application files and built APKs
COPY server/ ./server/
COPY Zynera*.apk ./

# Default environment configuration
ENV PORT=8080
ENV NODE_ENV=production

# Expose port for container networking
EXPOSE 8080

# Container Healthcheck for zero-downtime deploys
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD node -e "require('http').get('http://localhost:' + (process.env.PORT || 8080) + '/health', (r) => process.exit(r.statusCode === 200 ? 0 : 1))"

# Launch Zynera Music Backend & Web Portal
CMD ["node", "server/server.js"]
