describe('cypress connect test', () => {
    it('Open link', () => {
        cy.visit("http://localhost:3000")
    })
})

describe('Uploading file', () => {
     it('should be able to choose csv file for uploading', () => {
        cy.get('#csv-id').selectFile('cypress/fixtures/correctData.csv')
        cy.get('input[type="file"]').attachFile('correctData.csv');
     })

     it('should be able to drag and drop csv file for uploading', () => {
        cy.get('#csv-id').selectFile('cypress/fixtures/correctData.csv')
        cy.get('input[type="file"]').attachFile('correctData.csv', { subjectType: 'drag-n-drop' });
     })

     it('should not be able to redirect to addRules page if no file is selcted after clicking upload', () => {
        cy.on('uncaught:exception', () => {
            return false;
          });
        cy.reload()
        cy.get('#csv-submit-button').click()
        cy.url().should('be.equal', 'http://localhost:3000/')
     })

     it('should be able to redirect to addRules page after clicking upload', () => {
        cy.get('#csv-id').selectFile('cypress/fixtures/correctData.csv')
        cy.get('#csv-submit-button').click()
        cy.url().should('be.equal', 'http://localhost:3000/addRules.html')
     })
 })

describe('Setting validation rules', () => {
    it('should be able to fill the headers from the csv file', () => {
        cy.visit('http://localhost:3000')
        cy.get('#csv-id').selectFile('cypress/fixtures/correctData.csv')
        cy.get('#csv-submit-button').click()
        cy.get('#field0').should('have.value', 'Product Id')
        cy.get('#field1').should('have.value', 'Product Description')
        cy.get('#field2').should('have.value', 'Price')
        cy.get('#field3').should('have.value', 'Export')
        cy.get('#field4').should('have.value', 'Country Name')
        cy.get('#field5').should('have.value', 'Source City')
        cy.get('#field6').should('have.value', 'Country Code')
        cy.get('#field7').should('have.value', 'Source Pincode')
    })

    it('should be able to alert if mandatory fields are not filled', () => {
        cy.get('#upload-configs').click()
        cy.get('#alert-message').then(($message) => {
             expect($message).to.contain('Enter mandatory fields')
         })
        cy.get("#close").click()
    })

    it('should allow user to select and enter values in input box', () => {
        cy.get('#type0').select(1)
        cy.get("#fixed-len0").type("5")
        cy.get('#type1').select(8)
        cy.get('#type2').select(1)
        cy.get('#type3').select(3)
        cy.get('#fixed-len3').type("1")
        cy.get('#type4').select(3)
        cy.get('#type5').select(3)
        cy.get('#type6').select(1)   
        cy.get('#type7').select(1) 
    })
})

