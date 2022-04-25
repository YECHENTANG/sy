package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	private final class MyModel implements  Model{

		private final GameSetup setup;
		private Set<Observer> observers;
		private final Player mrX;
		private final ImmutableList<Player> detectives;
		private Board.GameState state;
        private MyGameStateFactory m=new MyGameStateFactory();
		MyModel(GameSetup setup, Player mrX, ImmutableList<Player> detectives){
			this.setup = setup;
			this.mrX = mrX;
			this.detectives = detectives;
			this.observers = new HashSet<>();
			this.state = m.build(setup,mrX,detectives);

		}
		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return this.state;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if(observers.contains(observer))
				throw new IllegalArgumentException("the observer has been register");
			if(observer == null)
				throw new NullPointerException();
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if(observer == null)
				throw new NullPointerException();
			if(!observers.contains(observer))
				throw new IllegalArgumentException("no observer be register");
			observers.remove(observer);
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			Observer.Event event;

			this.state = this.state.advance(move);

			if(!state.getWinner().isEmpty()){
				event = Observer.Event.GAME_OVER;
				}

			else {event = Observer.Event.MOVE_MADE;}

			for(Observer observer : observers){
				observer.onModelChanged(this.state,event);}

		}
	}

	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {

		MyModel mymodel = new MyModel(setup, mrX, detectives);
		return mymodel;
	}
}