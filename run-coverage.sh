#!/bin/bash
# Script to run tests and generate JaCoCo coverage report
# Then open the HTML report in the default browser

echo "============================================"
echo "Running Tests with JaCoCo Coverage"
echo "============================================"
echo ""

./mvnw clean test

if [ $? -ne 0 ]; then
    echo ""
    echo "============================================"
    echo "Tests Failed! Please check the errors above."
    echo "============================================"
    exit 1
fi

echo ""
echo "============================================"
echo "Tests Completed Successfully!"
echo "============================================"
echo ""

# Check if the coverage report was generated
if [ -f "target/site/jacoco/index.html" ]; then
    echo "============================================"
    echo "Opening Coverage Report in Browser..."
    echo "============================================"
    echo ""
    echo "Report Location: target/site/jacoco/index.html"
    echo ""

    # Detect OS and open accordingly
    case "$(uname -s)" in
        Darwin*)
            open target/site/jacoco/index.html
            ;;
        Linux*)
            xdg-open target/site/jacoco/index.html 2>/dev/null || echo "Please open target/site/jacoco/index.html manually"
            ;;
        CYGWIN*|MINGW*|MSYS*)
            start target/site/jacoco/index.html
            ;;
        *)
            echo "Please open target/site/jacoco/index.html manually"
            ;;
    esac

    echo ""
    echo "Coverage report opened in your default browser."
else
    echo "============================================"
    echo "Warning: Coverage report not found!"
    echo "============================================"
    echo "Expected location: target/site/jacoco/index.html"
fi

echo ""
echo "============================================"
echo "Coverage Summary Location:"
echo "  HTML: target/site/jacoco/index.html"
echo "  XML:  target/site/jacoco/jacoco.xml"
echo "  CSV:  target/site/jacoco/jacoco.csv"
echo "============================================"
echo ""

