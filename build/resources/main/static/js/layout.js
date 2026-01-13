document.addEventListener('DOMContentLoaded', function() {
    // Dropdown functionality for mobile or click interactions if needed
    // Currently using CSS hover for desktop, but we can add click toggle for touch devices
    
    const dropdowns = document.querySelectorAll('.nav-item.dropdown');
    
    dropdowns.forEach(dropdown => {
        dropdown.addEventListener('click', function(e) {
            if (window.innerWidth <= 768) { // Mobile behavior
                e.preventDefault();
                this.classList.toggle('active');
            }
        });
    });
});
