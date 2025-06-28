#!/bin/bash

# Set the base URL
BASE_URL="http://localhost:8081/api/v1/gt"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print test header
print_test_header() {
    echo -e "\n${YELLOW}=== $1 ===${NC}"
    echo -e "${YELLOW}Description: $2${NC}"
    echo -e "${YELLOW}----------------------------------------${NC}"
}

# Function to print success message
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print warning message
print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Function to print error message
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to make API request and print formatted response
make_request() {
    local url=$1
    local method=$2
    local data=$3
    
    echo -e "\n${YELLOW}Request:${NC}"
    echo "URL: $url"
    echo "Method: $method"
    echo "Data: $data"
    
    echo -e "\n${YELLOW}Response:${NC}"
    if [ -z "$data" ]; then
        curl -s -X $method "$url" | jq .
    else
        echo $data | curl -s -X $method -H "Content-Type: application/json" -d @- "$url" | jq .
    fi
    
    # Print a separator
    echo -e "\n${YELLOW}----------------------------------------${NC}"
}

# Check if jq is installed for JSON pretty printing
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required for this script. Please install jq first."
    exit 1
fi

# Test 1: Valid search query with travel details
print_test_header "Test 1" "Valid search query with travel details"
make_request "$BASE_URL/process-search" POST '{"searchInput":"I want to book a flight from San Francisco to New York for 2 passengers next week"}'

# Test 2: Minimum valid input (3 chars, 5 words)
print_test_header "Test 2" "Minimum valid input (3 chars, 5 words)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"a b c d e"}'

# Test 3: Input with special characters (should be allowed)
print_test_header "Test 3" "Input with special characters (should be allowed)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"Flight from SFO to JFK on 12/25 - 2 adults, 1 child"}'

# Test 4: Input with potential SQL injection (should be blocked)
print_test_header "Test 4" "Input with SQL injection (should be blocked)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"SQL injection attempt: SELECT * FROM users"}'

# Test 5: Input with potential XSS (should be blocked)
print_test_header "Test 5" "Input with XSS attempt (should be blocked)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"XSS attempt: <script>alert(1)</script>"}'

# Test 6: Input that's too short (should fail validation)
print_test_header "Test 6" "Input too short (should fail validation)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"hi"}'

# Test 7: Input with not enough words (should fail validation)
print_test_header "Test 7" "Not enough words (should fail validation)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"one two three four"}'

# Test 8: Input with profanity (should return warning but still process)
print_test_header "Test 8" "Input with profanity (should return warning but still process)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"I need a damn flight to Vegas"}'

# Test 9: Input with unusual characters (should return warning but still process)
print_test_header "Test 9" "Input with unusual characters (should return warning but still process)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"Fl!ght t0 N3w Y0rk $500"}'

# Test 10: Input missing travel context (should return warning but still process)
print_test_header "Test 10" "Input missing travel context (should return warning but still process)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"I need to go somewhere nice with good food"}'

# Test 11: Input with multiple warnings
print_test_header "Test 11" "Input with multiple warnings"
make_request "$BASE_URL/process-search" POST '{"searchInput":"@#!$%^&*()_+ this is a test with unusual chars and no travel context"}'

# Test 12: Empty input (should fail validation)
print_test_header "Test 12" "Empty input (should fail validation)"
make_request "$BASE_URL/process-search" POST '{"searchInput":""}'

# Test 13: Input with maximum allowed length (500 chars)
long_string=$(printf 'x%.0s' {1..500})
print_test_header "Test 13" "Input with maximum allowed length (500 chars)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"'"$long_string"'"}'

# Test 14: Input with excessive special characters (should be blocked)
print_test_header "Test 14" "Input with excessive special characters (should be blocked)"
make_request "$BASE_URL/process-search" POST '{"searchInput":"!!!!!! @@@@@@ ##### $$$$$ %%%%% ^^^^^ &&&&& *****"}'

echo -e "\n${GREEN}=== Testing Complete ===${NC}"

# Also test the search-filters endpoint
echo -e "\n${YELLOW}=== Testing search-filters endpoint ===${NC}"
make_request "$BASE_URL/search-filters?searchInput=Flight%20to%20Paris" GET
