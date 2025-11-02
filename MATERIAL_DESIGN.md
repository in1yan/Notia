# Rosé Pine Theme for Notia

## Overview
Notia now features a beautiful Rosé Pine dark theme with warm, muted colors and a cozy aesthetic perfect for long coding/writing sessions.

## Design Philosophy

Rosé Pine is a low-contrast dark theme that prioritizes:
- **Comfortable viewing** - Easy on the eyes
- **Warm palette** - Soft, muted colors
- **Clear hierarchy** - Good contrast where it matters
- **Aesthetic beauty** - Visually pleasing combinations

## Color Palette

### Base Colors
- **Base**: #191724 - Main background
- **Surface**: #1f1d2e - Slightly lighter background  
- **Overlay**: #26233a - Card/elevated surfaces
- **Muted**: #6e6a86 - Disabled/inactive text
- **Subtle**: #908caa - Secondary text
- **Text**: #e0def4 - Primary text

### Accent Colors
- **Love** (Red): #eb6f92 - Errors, delete actions
- **Gold** (Yellow): #f6c177 - Warnings, highlights
- **Rose** (Pink): #ebbcba - Primary accent, headings
- **Pine** (Teal): #31748f - Interactive elements
- **Foam** (Cyan): #9ccfd8 - Links, hover states
- **Iris** (Purple): #c4a7e7 - Code, special elements

### Typography
- **Font Family**: Segoe UI, Roboto, Helvetica Neue
- **Base Size**: 14px
- **Headings**: Bold, 16px+
- **Button Text**: Medium weight (500)

### Elevation System
Material Design uses shadows to create depth:

- **Level 1**: Cards, List items (2dp shadow)
- **Level 2**: Buttons (3dp shadow)
- **Level 3**: Toolbar (4dp shadow)
- **Level 4**: Hover states (6dp shadow)

## Component Styling

### Toolbar
- Blue (#2196F3) background
- White text and buttons
- Elevated with shadow
- Rounded buttons with hover effects

### List Views
- Card-based design
- Subtle shadows for depth
- Blue selection highlight
- Smooth hover animations
- 4px spacing between items

### Buttons
- Raised style with shadows
- Blue background (#2196F3)
- White text
- Hover: Darker blue + larger shadow
- Pressed: Darkest blue + smaller shadow
- Disabled: Grey with no shadow

### Text Areas
- White background
- Blue focus indicator (left border)
- 16px padding
- Clean, minimal borders

### Text Fields
- Underline-style borders
- Blue underline on focus
- Material Design padding
- Grey placeholder text

### Scroll Bars
- Minimal, rounded
- Grey when idle
- Darker on hover
- Transparent track

### Split Panes
- Thin grey dividers
- Blue on hover
- Smooth transitions

## Special Components

### Chat Sidebar
Enhanced Material Design:
- Gradient-like background
- Rounded message bubbles
- Blue user messages
- Grey AI messages
- Yellow system messages
- Smooth fade-in animations
- Circular progress indicator

### Sidebars
- Light grey background (#fafafa)
- Bold section headers
- Card-style list items
- Proper spacing and padding

## Responsive Design

### Hover States
All interactive elements have hover feedback:
- Darker colors
- Larger shadows
- Cursor changes to pointer

### Focus States
Input elements show clear focus:
- Blue accent color
- Underline or border highlight
- No harsh outlines

### Active States
Buttons and clickable items show when pressed:
- Darkest color variant
- Reduced shadow (pressed effect)

## Accessibility

### High Contrast
- Dark text on light backgrounds
- Blue accent for important elements
- Clear visual hierarchy

### Touch Targets
- Minimum 44px height for buttons
- Adequate padding for clickability
- Proper spacing between elements

## CSS Files

### material-theme.css
Main theme file containing:
- Color definitions
- Component styles
- Typography
- Elevation system
- Animations

### chat-styles.css
Chat-specific enhancements:
- Message bubble styles
- Loading animations
- Custom colors for message types
- Emoji font support

## Customization

### Changing Primary Color

Edit `material-theme.css`:
```css
.root {
    -fx-accent: #E91E63; /* Pink instead of Blue */
}

.tool-bar {
    -fx-background-color: #E91E63;
}

.button {
    -fx-background-color: #E91E63;
}

/* Update hover and pressed states accordingly */
```

### Adjusting Shadows

Modify elevation levels:
```css
/* Light shadow */
-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 2, 0, 0, 1);

/* Medium shadow */
-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 4, 0, 0, 2);

/* Heavy shadow */
-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 8, 0, 0, 4);
```

### Changing Fonts

Update font family:
```css
.root {
    -fx-font-family: 'Your Font', 'Fallback Font', sans-serif;
}
```

## Material Design Principles

### Visual Hierarchy
- Important elements are elevated
- Primary actions use accent color
- Secondary actions are subtle

### Consistency
- Same spacing units (8dp grid)
- Consistent border radius (4px)
- Unified color palette

### Motion
- Smooth transitions
- Meaningful animations
- Responsive feedback

### Layout
- 8dp spacing grid
- Proper padding and margins
- Balanced whitespace

## Browser Compatibility

The WebView preview uses Material-inspired HTML/CSS:
- UTF-8 encoding
- Clean typography
- Syntax-highlighted code blocks
- Responsive padding

## Performance

Optimizations:
- CSS loaded once at startup
- Hardware-accelerated shadows
- Minimal repaints
- Efficient selectors

## Future Enhancements

Potential improvements:
- Dark mode theme
- Custom accent color picker
- Theme presets (Blue, Pink, Green, etc.)
- Animation speed controls
- Compact/comfortable density options

## Resources

Material Design Guidelines:
- https://material.io/design
- https://material.io/components
- https://material.io/design/color
- https://material.io/design/typography

## Screenshots Description

### Main Window
- Blue toolbar with white buttons
- Card-style note list
- Clean split pane layout
- Minimal scrollbars

### Chat Interface
- Modern message bubbles
- Blue user messages
- Smooth animations
- Circular progress indicator

### Edit View
- Clean white text area
- Blue focus indicator
- Comfortable padding
- Minimal distractions

### Preview
- Professional typography
- Code block styling
- Proper spacing
- Readable fonts

Enjoy the modern Material Design experience! 🎨
