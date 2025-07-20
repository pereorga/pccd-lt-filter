#!/usr/bin/env node

const { spawn } = require("child_process");
const path = require("path");
const fs = require("fs");

// Get the JAR path relative to this script
const jarPath = path.join(__dirname, "lt-filter.jar");

// Check if JAR exists
if (!fs.existsSync(jarPath)) {
  console.error(`Error: lt-filter.jar not found at ${jarPath}`);
  process.exit(1);
}

// Check if Java is available
const { execSync } = require("child_process");
try {
  execSync("java -version", { stdio: "ignore" });
} catch (error) {
  console.error("Error: Java is not installed or not in PATH");
  console.error("Please install Java 17+ (e.g., 'apt install openjdk-17-jre-headless')");
  process.exit(1);
}

// Run the JAR with all command line arguments
const args = ["java", "-jar", jarPath, ...process.argv.slice(2)];
const child = spawn(args[0], args.slice(1), {
  stdio: "inherit",
  shell: false,
});

child.on("close", (code) => {
  process.exit(code || 0);
});

child.on("error", (error) => {
  console.error(`Failed to start lt-filter: ${error.message}`);
  process.exit(1);
});
