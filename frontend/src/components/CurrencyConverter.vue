<template>
    <div class="currency-converter">
      <h2>Currency Converter</h2>
      <form @submit.prevent="convertCurrency">
        <div class="form-row">
          <div class="form-group">
            <label for="sourceCurrency">Source Currency:</label>
            <input
              type="text"
              id="sourceCurrency"
              v-model="sourceCurrency"
              required
            />
          </div>
          <div class="form-group">
            <label for="targetCurrency">Target Currency:</label>
            <input
              type="text"
              id="targetCurrency"
              v-model="targetCurrency"
              required
            />
          </div>
        </div>
        <span v-if="!isSourceCurrencyValid || !isTargetCurrencyValid" class="error-message">
          Please enter valid 3-letter currency codes (e.g. EUR, USD).
        </span>
        <div class="form-group">
          <label for="amount">Amount:</label>
          <input
            type="number"
            id="amount"
            v-model="amount"
            step="0.01"
            required
          />
          <span v-if="!isAmountValid" class="error-message">
            Please enter a valid amount greater than 0.
          </span>
        </div>
        <button type="submit" class="convert-button" :disabled="!isFormValid">
          Convert
        </button>
      </form>
      <div class="result">
        <div class="form-group">
          <label for="convertedValue">Converted Value:</label>
          <input
            type="text"
            id="convertedValue"
            :value="formattedConvertedValue"
            readonly
          />
        </div>
        <span v-if="errorMessage" class="error-message">{{ errorMessage }}</span>
      </div>
    </div>
  </template>
  
  <script>
  import axios from "axios";
  
  export default {
    data() {
      return {
        sourceCurrency: "",
        targetCurrency: "",
        amount: null,
        convertedValue: null,
        rawConvertedValue: null,
        errorMessage: "",
        formattedConvertedValue: "",
      };
    },
    computed: {
      isSourceCurrencyValid() {
        const currencyRegex = /^[A-Z]{3}$/;
        return currencyRegex.test(this.sourceCurrency);
      },
      isTargetCurrencyValid() {
        const currencyRegex = /^[A-Z]{3}$/;
        return currencyRegex.test(this.targetCurrency);
      },
      isAmountValid() {
        return this.amount > 0;
      },
      isFormValid() {
        return (
          this.isSourceCurrencyValid &&
          this.isTargetCurrencyValid &&
          this.isAmountValid
        );
      },
    },
    methods: {
      async convertCurrency() {
        console.log("convertCurrency method called");
        const apiHost = import.meta.env.CURRENCY_CONVERTER_API_HOST;
        const apiPort = import.meta.env.CURRENCY_CONVERTER_API_PORT;
        const apiUrl = `${apiHost}:${apiPort}/convert/${this.sourceCurrency}/${this.targetCurrency}/${this.amount}`;
        console.log("API URL:", apiUrl);
        try {
          const response = await axios.get(apiUrl);
          console.log("API response:", response.data);
          this.rawConvertedValue = response.data.value;
          this.formattedConvertedValue = this.formatCurrency(this.rawConvertedValue, this.targetCurrency);
          this.errorMessage = "";
        } catch (error) {
          console.error("Error converting currency:", error);
          this.errorMessage = error.response?.data?.message || "Conversion failed. Please try again.";
          this.rawConvertedValue = null;
          this.formattedConvertedValue = "";
        }
      },
      formatCurrency(value, currency) {
        const currencyFormats = {
          USD: { locale: "en-US", options: { style: "currency", currency: "USD" } },
          EUR: { locale: "de-DE", options: { style: "currency", currency: "EUR" } },
          JPY: { locale: "ja-JP", options: { style: "currency", currency: "JPY" } },
          GBP: { locale: "en-GB", options: { style: "currency", currency: "GBP" } },
          AUD: { locale: "en-AU", options: { style: "currency", currency: "AUD" } },
          CAD: { locale: "en-CA", options: { style: "currency", currency: "CAD" } },
          CHF: { locale: "de-CH", options: { style: "currency", currency: "CHF" } },
          CNY: { locale: "zh-CN", options: { style: "currency", currency: "CNY" } },
          INR: { locale: "en-IN", options: { style: "currency", currency: "INR" } }
        };
        const format = currencyFormats[currency] || null;
        if (!format) {
          return value;
        }
        return new Intl.NumberFormat(format.locale, format.options).format(value);
      },
    },
  };
  </script>
  
  <style scoped>
  .currency-converter {
    max-width: 500px;
    min-height: 550px;
    margin: 0 auto;
    padding: 2rem;
    border: 1px solid #ccc;
    border-radius: 8px;
    background-color: #333;
    color: #fff;
  }
  
  h2 {
    text-align: center;
    margin-bottom: 1.5rem;
  }
  
  .form-row {
    display: flex;
    justify-content: space-between;
    gap: 1rem;
  }
  
  .form-group {
    flex: 1;
    margin-bottom: 1rem;
  }
  
  label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: bold;
  }
  
  input {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ccc;
    border-radius: 4px;
  }
  
  .error-message {
    color: #ff4d4d;
    font-size: 0.875rem;
    margin-top: 0.25rem;
    display: block;
    white-space: pre-wrap;
    max-width: 100%;
    word-wrap: break-word;
  }
  
  .convert-button {
    display: block;
    width: 100%;
    padding: 0.75rem;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 1rem;
    cursor: pointer;
    margin-top: 1rem;
  }
  
  .convert-button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
  
  .convert-button:hover:enabled {
    background-color: #0056b3;
  }
  
  .result {
    margin-top: 1.5rem;
    text-align: center;
  }
  </style>