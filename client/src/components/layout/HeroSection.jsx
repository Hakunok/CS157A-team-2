export function HeroSection({
  isLoggedIn,
  user,
  className,
  onExploreClick,
  onSignUpClick,
  onBrowseTopicsClick,
  ...props
}) {
  const handleExploreClick = () => {
    if (onExploreClick) {
      onExploreClick();
    } else {
      window.location.href = '/explore';
    }
  };

  const handleSignUpClick = () => {
    if (onSignUpClick) {
      onSignUpClick();
    } else {
      window.location.href = '/signup';
    }
  };

  const handleBrowseTopicsClick = () => {
    if (onBrowseTopicsClick) {
      onBrowseTopicsClick();
    } else {
      window.location.href = '/topics';
    }
  };

  return (
      <section className={cn("text-center py-12", className)} {...props}>
        {isLoggedIn ? (
            <div className="space-y-6">
              <h2 className="text-4xl font-ui font-bold text-balance">
                Welcome back{user?.fullName ? `, ${user.fullName.split(' ')[0]}` : ''}!
              </h2>
              <p className="text-xl text-muted-foreground max-w-2xl mx-auto text-balance">
                Here are some publications we think you'll be interested in
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <button
                    onClick={handleExploreClick}
                    className="ui-text px-6 py-3 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors font-medium"
                >
                  Explore More Papers
                </button>
                <button
                    onClick={handleBrowseTopicsClick}
                    className="ui-text px-6 py-3 border border-border text-foreground rounded-lg hover:bg-surface transition-colors font-medium"
                >
                  Browse by Topic
                </button>
              </div>
            </div>
        ) : (
            <>
              <h2 className="text-4xl font-ui font-bold mb-4 text-balance">
                Discover the Latest in AI Research
              </h2>
              <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto text-balance">
                Curated collection of cutting-edge papers, insights, and discussions from the artificial intelligence research community.
              </p>
              <div className="flex flex-col sm:flex-row gap-4 justify-center">
                <button
                    onClick={handleExploreClick}
                    className="ui-text px-6 py-3 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors font-medium"
                >
                  Explore Papers
                </button>
                <button
                    onClick={handleSignUpClick}
                    className="ui-text px-6 py-3 border border-border text-foreground rounded-lg hover:bg-surface transition-colors font-medium"
                >
                  Sign Up
                </button>
              </div>
            </>
        )}
      </section>
  );
}